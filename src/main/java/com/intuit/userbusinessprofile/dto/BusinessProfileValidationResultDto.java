package com.intuit.userbusinessprofile.dto;

import com.intuit.userbusinessprofile.model.Status;
import com.intuit.userbusinessprofile.model.TerminationReason;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serializable;

@Data
public class BusinessProfileValidationResultDto implements Serializable {
    private String validationId;
    private String profileId;
    private Long validationRequestEventTime;
    private Status status;
    @Nullable
    private String rejectionReason;
    @Nullable
    private String failureReason;
    @Nullable
    private TerminationReason terminationReason;
}
