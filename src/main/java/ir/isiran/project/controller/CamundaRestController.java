package ir.isiran.project.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/engine-rest")
public class CamundaRestController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${camunda.version}")
    private String camunda_version;

    @Value("${camunda.bpm.rest.mapper.base-url}")
    private String camunda_url;

    // GET /engine-rest/engine
    @GetMapping("/engine")
    public ResponseEntity<Map<String, Object>> getEngineInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Camunda External Engine");
        info.put("version", camunda_version);
        return ResponseEntity.ok(info);
    }

    // GET /engine-rest/process-definition
    @GetMapping("/process-definition")
    public ResponseEntity<List<Map<String, Object>>> getProcessDefinitions() {
        String url = camunda_url + "/process-definition";

        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> definitions = (List<Map<String, Object>>) response.getBody();

        List<Map<String, Object>> result = definitions.stream()
                .map(def -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", def.get("id"));
                    map.put("name", def.get("name"));
                    map.put("key", def.get("key"));
                    map.put("version", def.get("version"));
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/task")
    public ResponseEntity<List<Map<String, Object>>> getTasks() {
        String taskUrl = camunda_url + "/task";
        ResponseEntity<List> taskResponse = restTemplate.getForEntity(taskUrl, List.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) taskResponse.getBody();

        List<Map<String, Object>> result = new ArrayList<>();

        if (tasks != null) {
            for (Map<String, Object> task : tasks) {
                String taskId = (String) task.get("id");
                String getVariablesUrl = camunda_url + "/task/" + taskId + "/variables";

                ResponseEntity<Map> variableResponse = restTemplate.getForEntity(getVariablesUrl, Map.class);

                Map<String, Object> variables = variableResponse.getBody();

                // Convert typed variables to plain values
                Map<String, Object> flatVariables = new HashMap<>();
                if (variables != null) {
                    for (Object key : variables.keySet()) {
                        Object valueObj = variables.get(key);
                        if (valueObj instanceof Map) {
                            Map<String, Object> valueMap = (Map<String, Object>) valueObj;
                            Object value = valueMap.get("value");
                            flatVariables.put((String) key, value);
                        }
                    }
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", task.get("id"));
                map.put("name", task.get("name"));
                map.put("created", task.get("created"));
                map.put("variables", flatVariables);

                result.add(map);
            }
        }

        return ResponseEntity.ok(result);
    }

    // DELETE /engine-rest/process-definition
    @DeleteMapping("/process-definition")
    public ResponseEntity<Map<String, Object>> removeAllProcessDefinitions() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Step 1: Get all active process definitions
            ResponseEntity<List> defResponse = restTemplate.getForEntity(camunda_url + "/process-definition",
                    List.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> definitions = (List<Map<String, Object>>) defResponse.getBody();

            if (definitions == null || definitions.isEmpty()) {
                response.put("message", "No process definitions found.");
                return ResponseEntity.ok(response);
            }

            // Step 2: Delete each process definition by ID with cascade
            for (Map<String, Object> def : definitions) {
                String id = (String) def.get("id");
                String deleteUrl = camunda_url + "/process-definition/" + id + "?cascade=true";
                restTemplate.delete(deleteUrl);
                System.out.println("Deleted process definition: " + id);
            }

            // Optional: Delete deployments
            ResponseEntity<List> deploymentResponse = restTemplate.getForEntity(camunda_url + "/deployment",
                    List.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> deployments = (List<Map<String, Object>>) deploymentResponse.getBody();

            if (deployments != null) {
                for (Map<String, Object> dep : deployments) {
                    String id = (String) dep.get("id");
                    String deleteUrl = camunda_url + "/deployment/" + id + "?cascade=true";
                    restTemplate.delete(deleteUrl);
                    System.out.println("Deleted deployment: " + id);
                }
            }

            response.put("message", "All process definitions and deployments have been removed.");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }
}