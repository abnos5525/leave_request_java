package ir.isiran.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import ir.isiran.project.service.ProcessService;
import ir.isiran.project.util.ProcessConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired
    private ProcessService processService;

    @Value("${camunda.bpm.rest.mapper.base-url}")
    private String camunda_url;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/task")
    public ResponseEntity<List<Map<String, Object>>> getAllLeaving() {
        try {
            // Filter tasks by process definition key
            String taskUrl = camunda_url + "/task?processDefinitionKey=" + ProcessConstants.LEAVE_PROCESS;
            ResponseEntity<List> taskResponse = restTemplate.getForEntity(taskUrl, List.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) taskResponse.getBody();
            List<Map<String, Object>> result = new ArrayList<>();

            if (tasks != null) {
                for (Map<String, Object> task : tasks) {
                    String taskId = (String) task.get("id");

                    // Get task variables
                    String getVariablesUrl = camunda_url + "/task/" + taskId + "/variables";
                    ResponseEntity<Map> variableResponse = restTemplate.getForEntity(getVariablesUrl, Map.class);
                    Map<String, Object> variables = variableResponse.getBody();

                    // Process variables
                    Map<String, Object> flatVariables = new HashMap<>();
                    if (variables != null) {
                        variables.forEach((key, value) -> {
                            if (value instanceof Map) {
                                Map<String, Object> valueMap = (Map<String, Object>) value;
                                flatVariables.put(key, valueMap.get("value"));
                            }
                        });
                    }

                    // Build response
                    Map<String, Object> taskInfo = new LinkedHashMap<>();
                    taskInfo.put("id", task.get("id"));
                    taskInfo.put("name", task.get("name"));
                    taskInfo.put("created", task.get("created"));
                    taskInfo.put("variables", flatVariables);

                    result.add(taskInfo);
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Failed to retrieve leave tasks: " + e.getMessage())));
        }
    }

    @PostMapping("/start-leave-process")
    public ResponseEntity<Map<String, Object>> startLeaveProcess(
            @RequestParam(name = "leaveType") @Parameter(description = "Type of leave", required = true, schema = @Schema(type = "string", allowableValues = {
                    "hour", "day" }, defaultValue = "hour", example = "hour")) String leaveType,
            @RequestParam(name = "leaveAmount") @Parameter(description = "Duration of leave (positive number)", required = true, schema = @Schema(type = "number", format = "int64", minimum = "1", maximum = "365", example = "5")) @Min(1) @Max(365) Long leaveAmount,
            Authentication authentication) {

        if (!"hour".equalsIgnoreCase(leaveType) && !"day".equalsIgnoreCase(leaveType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid leaveType. Must be either 'hour' or 'day'"));
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: No authentication found"));
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String employeeName = jwt.getClaimAsString("name");
        String employeeId = jwt.getClaimAsString("sub");

        processService.deploy();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", employeeName);
        variables.put("employeeId", employeeId);
        variables.put("leaveType", leaveType);
        variables.put("leaveAmount", leaveAmount);
        variables.put("approved", "Unchecked");

        // Start the process and get the process instance
        String processInstanceId = processService.startProcessInstance(ProcessConstants.LEAVE_PROCESS, variables);

        // Immediately complete the employee submission task
        processService.completeFirstTask(processInstanceId);

        return ResponseEntity.ok()
                .body(Map.of(
                        "processInstanceId", processInstanceId,
                        "variables", variables));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping("/manager/decision")
    @Operation(summary = "Approve or reject leave request", description = "Manager decision endpoint for leave requests")
    public ResponseEntity<Map<String, Object>> managerDecision(
            @RequestParam(name = "taskId") @Parameter(description = "ID of the task to complete", required = true) String taskId,
            @RequestParam(name = "decision") @Parameter(description = "Manager's decision", required = true, schema = @Schema(type = "string", allowableValues = {
                    "approve", "reject" }, example = "approve")) String decision,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: No authentication found"));
        }

        try {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String managerName = jwt.getClaimAsString("name");
            String managerId = jwt.getClaimAsString("sub");

            String taskUrl = camunda_url + "/task/" + taskId;
            ResponseEntity<Map> taskResponse = restTemplate.getForEntity(taskUrl, Map.class);

            if (taskResponse.getStatusCode() != HttpStatus.OK || taskResponse.getBody() == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
            }

            String taskName = (String) taskResponse.getBody().get("name");
            if (!"Manager Approval".equals(taskName)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "This is not a manager approval task"));
            }

            boolean isApproved = "approve".equalsIgnoreCase(decision);

            Map<String, Object> decisionVariables = new HashMap<>();
            decisionVariables.put("approved", Map.of(
                    "value", isApproved,
                    "type", "Boolean"));

            // Add manager details to variables
            decisionVariables.put("managerName", Map.of(
                    "value", managerName,
                    "type", "String"));

            decisionVariables.put("managerId", Map.of(
                    "value", managerId,
                    "type", "String"));

            String completeUrl = camunda_url + "/task/" + taskId + "/complete";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("variables", decisionVariables);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(completeUrl, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.ok(Map.of(
                        "message", "Decision processed successfully",
                        "decision", isApproved ? "approved" : "rejected",
                        "approved", isApproved,
                        "taskId", taskId,
                        "manager", Map.of(
                                "name", managerName,
                                "id", managerId)));
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body(Map.of("error", "Failed to complete task: " + response.getBody()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error processing decision: " + e.getMessage()));
        }
    }

    @GetMapping("/completed-requests")
    public ResponseEntity<List<Map<String, Object>>> getCompletedLeaveRequests() {
        try {
            // Query completed process instances sorted by end time (newest first)
            String historyUrl = camunda_url + "/history/process-instance?" +
                    "processDefinitionKey=" + ProcessConstants.LEAVE_PROCESS + "&finished=true" +
                    "&sortBy=endTime&sortOrder=desc";
            ResponseEntity<List> instanceResponse = restTemplate.getForEntity(historyUrl, List.class);

            List<Map<String, Object>> result = new ArrayList<>();

            if (instanceResponse.getBody() != null) {
                for (Map<String, Object> instance : (List<Map<String, Object>>) instanceResponse.getBody()) {
                    String processInstanceId = (String) instance.get("id");

                    // Get variables for this process instance
                    String variablesUrl = camunda_url + "/history/variable-instance?processInstanceId="
                            + processInstanceId;
                    ResponseEntity<List> variablesResponse = restTemplate.getForEntity(variablesUrl, List.class);

                    // Extract relevant data
                    Map<String, Object> leaveData = new LinkedHashMap<>();
                    boolean isApproved = false;
                    String status = "Pending";
                    String managerName = "Unknown";
                    String managerId = "Unknown";

                    if (variablesResponse.getBody() != null) {
                        for (Map<String, Object> var : (List<Map<String, Object>>) variablesResponse.getBody()) {
                            String varName = (String) var.get("name");
                            Object varValue = var.get("value");

                            // Handle each variable type appropriately
                            if ("employeeName".equals(varName) ||
                                    "employeeId".equals(varName) ||
                                    "leaveType".equals(varName) ||
                                    "leaveAmount".equals(varName)) {
                                leaveData.put(varName, varValue);
                            } else if ("approved".equals(varName)) {
                                if (varValue instanceof Boolean) {
                                    isApproved = (Boolean) varValue;
                                } else if (varValue instanceof String) {
                                    isApproved = Boolean.parseBoolean((String) varValue);
                                } else if (varValue != null) {
                                    isApproved = Boolean.parseBoolean(varValue.toString());
                                }
                                status = isApproved ? "Approved" : "Rejected";
                            } else if ("managerName".equals(varName)) {
                                managerName = (varValue != null) ? varValue.toString() : "Unknown";
                            } else if ("managerId".equals(varName)) {
                                managerId = (varValue != null) ? varValue.toString() : "Unknown";
                            }
                        }
                    }

                    // Build the final response object
                    Map<String, Object> responseItem = new LinkedHashMap<>();
                    responseItem.put("employee", Map.of(
                            "name", leaveData.getOrDefault("employeeName", "Unknown"),
                            "id", leaveData.getOrDefault("employeeId", "Unknown")));
                    responseItem.put("request", Map.of(
                            "type", leaveData.getOrDefault("leaveType", "Unknown"),
                            "amount", leaveData.getOrDefault("leaveAmount", 0)));
                    responseItem.put("decision", Map.of(
                            "status", status,
                            "approved", isApproved,
                            "date", instance.get("endTime"),
                            "by", Map.of(
                                    "name", managerName,
                                    "id", managerId
                            )));
                    responseItem.put("processInstanceId", processInstanceId);

                    result.add(responseItem);
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of(Map.of("error", "Failed to retrieve completed requests: " + e.getMessage())));
        }
    }

}
