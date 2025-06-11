package ir.isiran.project.delegate;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class AutomaticApprovalDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Extract variables from execution context
        String employeeName = (String) execution.getVariable("employeeName");
        String employeeId = (String) execution.getVariable("employeeId");
        String leaveType = (String) execution.getVariable("leaveType");
        Double leaveAmount = (Double) execution.getVariable("leaveAmount");
        Boolean approved = (Boolean) execution.getVariable("approved");

        // Log the request
        System.out.println("Saved request: " + employeeName);

        // Optional: Build a response map for internal use/logging
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("employeeName", employeeName);
        responseMap.put("employeeId", employeeId);
        responseMap.put("leaveType", leaveType);
        responseMap.put("leaveAmount", leaveAmount);
        responseMap.put("approved", approved);

        // You can't return this map directly, but you can:
        // 1. Set it as a process variable for later use
        execution.setVariable("approvalResult", responseMap);
    }
}