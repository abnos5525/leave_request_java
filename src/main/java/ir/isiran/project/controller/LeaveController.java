package ir.isiran.project.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import ir.isiran.project.dto.LeaveRequestDto;
import ir.isiran.project.service.ProcessDeploymentService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired
    private ProcessDeploymentService processDeploymentService;

    @PostMapping("/start-leave-process")
    public ResponseEntity<Map<String, Object>> startLeaveProcess(
            @RequestBody LeaveRequestDto dto,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: No authentication found"));
        }

        // Extract principal from authentication
        Jwt jwt = (Jwt) authentication.getPrincipal();

        // Get user name and ID from claims
        String employeeName = jwt.getClaimAsString("name");
        String employeeId = jwt.getClaimAsString("sub");

        // Deploy process definition if not already deployed
        processDeploymentService.deploy();

        // Prepare process variables
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", employeeName);
        variables.put("employeeId", employeeId);
        variables.put("leaveType", dto.getLeaveType());
        variables.put("leaveAmount", dto.getAmount());
        variables.put("approved", false);

        // Start process instance using REST API
        processDeploymentService.startProcessInstance("Leave_process", variables);

        // Return response
        return ResponseEntity.ok()
                .body(Map.of(
                        "message", "Process started successfully",
                        "variables", variables));
    }
}
