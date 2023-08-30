package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.model.RetryProfileValidationRequest;
import com.intuit.userbusinessprofile.repository.RetryProfileValidationRequestRepository;
import com.intuit.userbusinessprofile.service.RetryProfileValidationRequestService;
import org.springframework.stereotype.Service;

@Service
public class RetryProfileValidationRequestServiceImpl implements RetryProfileValidationRequestService {

    private final RetryProfileValidationRequestRepository retryProfileValidationRequestRepository;

    public RetryProfileValidationRequestServiceImpl(RetryProfileValidationRequestRepository retryProfileValidationRequestRepository) {
        this.retryProfileValidationRequestRepository = retryProfileValidationRequestRepository;
    }

    @Override
    public RetryProfileValidationRequest createRetryProfileValidationRequest(RetryProfileValidationRequest retryProfileValidationRequest) {
        return retryProfileValidationRequestRepository.createRetryProfileValidationRequest(retryProfileValidationRequest);
    }
}
