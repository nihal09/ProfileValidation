package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.dto.ProductValidationResultDto;
import com.intuit.userbusinessprofile.model.Product;
import org.springframework.stereotype.Service;

@Service
public class DummyApiClient {

    public ProductValidationResultDto validateProfileForAProduct(BusinessProfileCreateUpdateValidationRequestDto validationRequest, Product product) {
        // Simulate validation logic here
        switch (product){
            case QB -> {
                // call QB validation api
            } case T_SHEETS -> {
                // call T_SHEETS validation api
            } case QB_PAYROLL -> {
                // call QB_PAYROLL validation api
            } case QB_PAYMENTS -> {
                // call QB_PAYMENTS validation api
            }
        }
        boolean isValid = Math.random() < 0.1; // Example: randomly determine validity
        String rejectionReason = isValid ? null : "Random reason";
        return new ProductValidationResultDto(true,rejectionReason, product);

    }

}
