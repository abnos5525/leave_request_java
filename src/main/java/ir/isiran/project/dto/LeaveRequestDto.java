package ir.isiran.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for submitting leave request variables")
public class LeaveRequestDto {

    @Schema(description = "Type of leave (day, hour)", example = "hour")
    private String leaveType;

    @Schema(description = "Duration of leave in hours or days", example = "1")
    private Double amount;

    // Getters and setters
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}