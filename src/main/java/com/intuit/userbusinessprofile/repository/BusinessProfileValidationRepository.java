package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class BusinessProfileValidationRepository {

    private final DynamoDBMapper dynamoDBMapper;


    public BusinessProfileValidationRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @CachePut(value = "BusinessProfileValidation",key = "#businessProfileValidation.validationId")
    public BusinessProfileValidation createBusinessProfileValidationTask(BusinessProfileValidation businessProfileValidation) {
        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression()
                .withExpectedEntry("validationId", new ExpectedAttributeValue(false));

        try {
            dynamoDBMapper.save(businessProfileValidation, saveExpression);
            return businessProfileValidation;
        } catch (ConditionalCheckFailedException e) {
            return dynamoDBMapper.load(BusinessProfileValidation.class, businessProfileValidation.getValidationId());
        }
    }
    @CachePut(value = "BusinessProfileValidation",key = "#businessProfileValidation.validationId")
    public void updateBusinessProfileValidationTask(BusinessProfileValidation businessProfileValidation) {
        businessProfileValidation.setUpdatedAt(Instant.now().toEpochMilli());
        dynamoDBMapper.save(businessProfileValidation);
    }

    @Cacheable(value = "BusinessProfileValidation",key = "#validationId")
    public BusinessProfileValidation getBusinessProfileValidation(String validationId){
        return dynamoDBMapper.load(BusinessProfileValidation.class, validationId);
    }

    @CachePut(value = "BusinessProfileValidation",key = "#validationId")
    public BusinessProfileValidation getBusinessProfileValidationAndUpdateCache(String validationId){
        System.out.println("BusinessProfileValidation-----------------\n-------------\n----------");
        return dynamoDBMapper.load(BusinessProfileValidation.class, validationId);
    }
}
