package ir.isiran.project.service;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ZeebeProcessService {

    @Autowired
    private ZeebeClient zeebeClient;

    public ProcessInstanceEvent startProcess(String bpmnProcessId, Map<String, Object> variables) {
        return zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();
    }

    public void completeTask(long jobKey, Map<String, Object> variables) {
        zeebeClient.newCompleteCommand(jobKey)
                .variables(variables)
                .send()
                .join();
    }
} 