package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.intuit.userbusinessprofile.model.RetryProfileValidationRequest;
import org.springframework.stereotype.Repository;

@Repository
public class RetryProfileValidationRequestRepository {

    private final DynamoDBMapper dynamoDBMapper;


    public RetryProfileValidationRequestRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public RetryProfileValidationRequest createRetryProfileValidationRequest(RetryProfileValidationRequest retryProfileValidationRequest){
        dynamoDBMapper.save(retryProfileValidationRequest);
        return retryProfileValidationRequest;
    }

}
