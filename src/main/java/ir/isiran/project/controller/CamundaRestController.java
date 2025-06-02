package ir.isiran.project.controller;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/engine-rest")
public class CamundaRestController {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Value("${camunda.version}")
    private String camunda_version;

    @GetMapping("/engine")
    public ResponseEntity<Map<String, Object>> getEngineInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", processEngine.getName());
        info.put("version", camunda_version);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/process-definition")
    public ResponseEntity<List<Map<String, Object>>> getProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
            .active()
            .list();

        List<Map<String, Object>> result = definitions.stream()
            .map(def -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", def.getId());
                map.put("name", def.getName());
                map.put("key", def.getKey());
                map.put("version", def.getVersion());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/task")
    public ResponseEntity<List<Map<String, Object>>> getTasks() {
        List<Task> tasks = taskService.createTaskQuery()
            .active()
            .list();

        List<Map<String, Object>> result = tasks.stream()
            .map(task -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", task.getId());
                map.put("name", task.getName());
                map.put("assignee", task.getAssignee());
                map.put("created", task.getCreateTime());
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
} 