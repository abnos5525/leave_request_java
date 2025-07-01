package ir.isiran.project.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import ir.isiran.project.util.ProcessConstants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcessService {

    @Value("${camunda.bpm.rest.mapper.base-url}")
    private String camunda_url;

    private final RestTemplate restTemplate = new RestTemplate();

    public void deploy() {
        String camundaRestApi = camunda_url + "/deployment/create";

        // Check if already deployed
        if (isProcessDeployed(ProcessConstants.LEAVE_PROCESS)) {
            System.out.println("Process" + ProcessConstants.LEAVE_PROCESS +"is already deployed.");
            return;
        }

        try {
            Resource resource = new ClassPathResource("processes/" + ProcessConstants.LEAVE_PROCESS +".bpmn");

            // Create multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Build request entity
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("deployment-name", "Leave Process Deployment");
            body.add("process", new FileSystemResource(resource.getFile()));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(camundaRestApi, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Process deployed successfully to Camunda (port 8181)");
            } else {
                System.err.println("Failed to deploy process. Response: " + response.getBody());
            }

        } catch (IOException e) {
            System.err.println("Error deploying process: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isProcessDeployed(String processKey) {
        String queryUrl = camunda_url + "/process-definition";
        ResponseEntity<String> response = restTemplate.getForEntity(queryUrl, String.class);
        return response.getBody() != null && response.getBody().contains(processKey);
    }

    public String startProcessInstance(String processDefinitionKey, Map<String, Object> variables) {
        String startProcessUrl = camunda_url + "/process-definition/key/" + processDefinitionKey + "/start";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap variables in Camunda's expected format
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("variables", convertToTypedVariables(variables));
        requestBody.put("businessKey", "leave_request_" + System.currentTimeMillis()); // Optional business key

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(startProcessUrl, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String processInstanceId = (String) response.getBody().get("id");
                System.out.println("Process instance started successfully. ID: " + processInstanceId);
                return processInstanceId;
            } else {
                throw new RuntimeException("Failed to start process. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error starting process instance: " + e.getMessage());
            throw new RuntimeException("Failed to start process instance", e);
        }
    }

    public void completeFirstTask(String processInstanceId) {
        try {
            String tasksUrl = camunda_url + "/task?processInstanceId=" + processInstanceId;
            ResponseEntity<List> response = restTemplate.getForEntity(tasksUrl, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> tasks = (List<Map<String, Object>>) response.getBody();

                if (!tasks.isEmpty()) {
                    String taskId = (String) tasks.get(0).get("id");
                    String completeUrl = camunda_url + "/task/" + taskId + "/complete";
                    restTemplate.postForEntity(completeUrl, Collections.emptyMap(), Void.class);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Error completing initial task: " + e.getMessage());
        }
    }

    public Map<String, Map<String, Object>> convertToTypedVariables(Map<String, Object> variables) {
        Map<String, Map<String, Object>> typedVariables = new HashMap<>();

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Map<String, Object> variable = new HashMap<>();
            variable.put("value", entry.getValue());

            if (entry.getValue() instanceof Boolean) {
                variable.put("type", "Boolean");
            } else if (entry.getValue() instanceof String) {
                variable.put("type", "String");
            } else if (entry.getValue() instanceof Number) {
                variable.put("type", "Double");
            }

            typedVariables.put(entry.getKey(), variable);
        }

        return typedVariables;
    }
}