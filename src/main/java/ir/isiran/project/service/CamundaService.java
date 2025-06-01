package ir.isiran.project.service;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CamundaService {

    @Autowired
    private RuntimeService runtimeService;

    public void startProcess() {
        runtimeService.startProcessInstanceByKey("Process_1");
    }
}
