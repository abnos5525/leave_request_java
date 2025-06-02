package ir.isiran.project.controller;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.keycloak.KeycloakPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ir.isiran.project.dto.LeaveRequestDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/camunda")
public class ProcessController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @GetMapping("/process-exists")
    public ResponseEntity<Boolean> checkProcessExists() {
        boolean exists = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("Leave_process")
                .latestVersion()
                .count() > 0;

        return ResponseEntity.ok(exists);
    }

    @PostMapping("/start-leave-process")
    public ResponseEntity<Map<String, Object>> startLeaveProcess(
            @RequestBody LeaveRequestDto dto,
            @AuthenticationPrincipal KeycloakPrincipal<?> keycloakPrincipal) {

        if (keycloakPrincipal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized: No Keycloak Principal found"));
        }

        String employeeName = keycloakPrincipal.getName(); // or getPreferredUsername()
        String employeeId = keycloakPrincipal.getKeycloakSecurityContext().getToken().getSubject();

        Map<String, Object> variables = new HashMap<>();
        variables.put("employeeName", employeeName);
        variables.put("employeeId", employeeId);
        variables.put("leaveType", dto.getLeaveType());
        variables.put("amount", dto.getAmount());

        var processInstance = runtimeService.startProcessInstanceByKey("Leave_process", variables);

        Map<String, Object> response = new HashMap<>();
        response.put("processInstanceId", processInstance.getId());
        response.put("submittedVariables", variables);

        return ResponseEntity.ok(response);
    }
}
