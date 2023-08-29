package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.model.User;

import java.util.concurrent.ExecutionException;

public interface BusinessProfileValidationService {

    void validateAndUpdateBusinessProfileIfRequired(BusinessProfileCreateUpdateValidationRequestDto request) throws ExecutionException, InterruptedException;

    BusinessProfileValidation getBusinessProfileValidation(String validationId);
    User test(String key);
}
