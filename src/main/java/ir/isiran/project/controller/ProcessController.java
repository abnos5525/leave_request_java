package ir.isiran.project.controller;

import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import ir.isiran.project.dto.LeaveRequestDto;
import ir.isiran.project.service.ZeebeProcessService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/camunda")
public class ProcessController {

    @Autowired
    private ZeebeProcessService zeebeProcessService;

    @PostMapping("/start-leave-process")
    public ResponseEntity<Map<String, Object>> startLeaveProcess(
            @RequestBody LeaveRequestDto dto,
            @AuthenticationPrincipal KeycloakPrincipal<?> keycloakPrincipal) {

        if (keycloakPrincipal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: No Keycloak Principal found"));
        }

        String employeeName = keycloakPrincipal.getName();
        String employeeId = keycloakPrincipal.getKeycloakSecurityContext().getToken().getSubject();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", employeeName);
        variables.put("employeeId", employeeId);
        variables.put("leaveType", dto.getLeaveType());
        variables.put("amount", dto.getAmount());

        ProcessInstanceEvent event = zeebeProcessService.startProcess("Leave_process", variables);

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceKey", event.getProcessInstanceKey());
        response.put("submittedVariables", variables);

        return ResponseEntity.ok(response);
    }
}