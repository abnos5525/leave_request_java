package ir.isiran.project.listener;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;

@Component
public class ManagerApprovalTaskListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        // Get the form variables
        Object approveButtonPressed = delegateTask.getVariable("Field_1q2dpuk");
        Object rejectButtonPressed = delegateTask.getVariable("Field_0i2bfc0");
        
        // Set the approved variable based on which button was pressed
        // In Camunda forms, if a button is pressed, it will have a value
        boolean approved = approveButtonPressed != null;
        
        delegateTask.setVariable("approved", approved);
        
        if (approved) {
            delegateTask.setVariable("approvalReason", "Approved by manager");
            System.out.println("Leave request approved by manager for employee: " + delegateTask.getVariable("employeeName"));
        } else {
            delegateTask.setVariable("approvalReason", "Rejected by manager");
            System.out.println("Leave request rejected by manager for employee: " + delegateTask.getVariable("employeeName"));
        }
    }
} 