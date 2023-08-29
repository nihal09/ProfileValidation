package com.intuit.userbusinessprofile.dto;

import com.intuit.userbusinessprofile.model.Product;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class ValidationResultDto {
    Boolean isValid;
    @Nullable
    String rejectionReason;

    public ValidationResultDto(Boolean isValid, @Nullable String rejectionReason) {
        this.isValid = isValid;
        this.rejectionReason = rejectionReason;
    }
}
