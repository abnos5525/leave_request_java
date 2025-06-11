package ir.isiran.project.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessDeploymentService {

    @Value("${camunda.bpm.rest.mapper.base-url}")
    private String camunda_url;

    private final RestTemplate restTemplate = new RestTemplate();

    public void deploy() {
        String camundaRestApi = camunda_url + "/deployment/create";

        // Check if already deployed
        if (isProcessDeployed("Leave_process")) {
            System.out.println("Process 'Leave_process' is already deployed.");
            return;
        }

        try {
            Resource resource = new ClassPathResource("processes/leave_process.bpmn");

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

    public void startProcessInstance(String processDefinitionKey, Map<String, Object> variables) {
        String startProcessUrl = camunda_url + "/process-definition/key/" + processDefinitionKey
                + "/start";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Wrap variables in Camunda's expected format
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("processVariables", convertToTypedVariables(variables));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(startProcessUrl, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Process instance started successfully: " + response.getBody());
        } else {
            System.err.println("Failed to start process. Response: " + response.getBody());
        }
    }

    // Helper: Convert plain map to Camunda-typed variables
    private Map<String, Map<String, Object>> convertToTypedVariables(Map<String, Object> variables) {
        Map<String, Map<String, Object>> typedVariables = new HashMap<>();

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            Map<String, Object> variable = new HashMap<>();
            variable.put("value", entry.getValue());

            // Optionally add type info if needed
            if (entry.getValue() instanceof String) {
                variable.put("type", "String");
            } else if (entry.getValue() instanceof Number) {
                variable.put("type", "Double");
            } else {
                variable.put("type", entry.getValue().getClass().getSimpleName());
            }

            typedVariables.put(entry.getKey(), variable);
        }

        return typedVariables;
    }
}