package ir.isiran.project.service;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;

import javax.annotation.PostConstruct;

@Service
public class ProcessDeploymentService {

    @Autowired
    private RepositoryService repositoryService;

    @PostConstruct
    public void deployProcesses() {
        try {
            // Deploy login process
            Resource resource = new ClassPathResource("login.bpmn");
            Deployment deployment = repositoryService.createDeployment()
                .addInputStream("login.bpmn", resource.getInputStream())
                .name("Login Process Deployment")
                .deploy();
            
            System.out.println("Deployed process: " + deployment.getName());
        } catch (IOException e) {
            System.err.println("Error deploying processes: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 