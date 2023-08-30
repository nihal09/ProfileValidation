package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.dto.BusinessProfileValidateAndCreateRequestDto;
import com.intuit.userbusinessprofile.dto.BusinessProfileValidateAndUpdateRequestDto;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.model.User;

import java.util.concurrent.ExecutionException;

public interface BusinessProfileValidationService {

    void validateAndUpdateBusinessProfileIfRequired(BusinessProfileCreateUpdateValidationRequestDto request) throws ExecutionException, InterruptedException;

    BusinessProfileValidation updateBusinessProfileValidationTask(BusinessProfileValidation businessProfileValidation);

    void initiateValidationForBusinessProfileCreation(BusinessProfileValidateAndCreateRequestDto requestDto, String validationId);

    void initiateValidationForBusinessProfileUpdation(BusinessProfileValidateAndUpdateRequestDto requestDto, String validationId);

    User test(String key);
}
