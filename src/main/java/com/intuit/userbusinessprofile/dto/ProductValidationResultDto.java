package com.intuit.userbusinessprofile.dto;

import com.intuit.userbusinessprofile.model.Product;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class ProductValidationResultDto {
    Boolean isValid;
    Product product;
    @Nullable
    String rejectionReason;

    public ProductValidationResultDto(Boolean isValid, @Nullable String rejectionReason, Product product) {
        this.isValid = isValid;
        this.rejectionReason = rejectionReason;
        this.product = product;
    }

    @Override
    public String toString() {
        return "Product - "+this.product+", Status - "+this.isValid+", RejectionReason - "+this.rejectionReason;
    }
}
