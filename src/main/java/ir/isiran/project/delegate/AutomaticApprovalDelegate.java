package ir.isiran.project.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class AutomaticApprovalDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Automatically approve the leave request for requests under 2 hours
        execution.setVariable("approved", true);
        execution.setVariable("approvalReason", "Automatically approved for hourly leave under 2 hours");
        
        System.out.println("Leave request automatically approved for employee: " + execution.getVariable("employeeName"));
    }
} 