package com.intuit.userbusinessprofile.service;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import com.intuit.userbusinessprofile.model.BusinessProfileHistory;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final DynamoDBMapper dynamoDBMapper;

    @Autowired
    public TransactionService(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public void createProfileUpdateUserAndValidationTaskInTransaction(
            User updatedUser,
            BusinessProfile newBusinessProfile,
            BusinessProfileValidation updatedBusinessProfileValidation
    ){
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
        transactionWriteRequest.addUpdate(newBusinessProfile);
        transactionWriteRequest.addUpdate(updatedUser);
        transactionWriteRequest.addUpdate(updatedBusinessProfileValidation);
        dynamoDBMapper.transactionWrite(transactionWriteRequest);
    }

    public void updateProfileAndTaskCreateProfileHistoryInTransaction(
            BusinessProfileHistory businessProfileHistory,
            BusinessProfile updatedBusinessProfile,
            BusinessProfileValidation updatedBusinessProfileValidation
    ){
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
        transactionWriteRequest.addUpdate(businessProfileHistory);
        transactionWriteRequest.addUpdate(updatedBusinessProfile);
        transactionWriteRequest.addUpdate(updatedBusinessProfileValidation);
        dynamoDBMapper.transactionWrite(transactionWriteRequest);
    }
}