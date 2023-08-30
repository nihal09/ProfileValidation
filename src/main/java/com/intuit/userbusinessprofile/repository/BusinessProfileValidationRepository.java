package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class BusinessProfileValidationRepository {

    private final DynamoDBMapper dynamoDBMapper;


    public BusinessProfileValidationRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

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
        dynamoDBMapper.save(businessProfileValidation);
    }

    @Cacheable(value = "BusinessProfileValidation",key = "#validationId",unless = "#result eq null or #result.profileId ne null")
    public BusinessProfileValidation getBusinessProfileValidation(String validationId){
        return dynamoDBMapper.load(BusinessProfileValidation.class, validationId);
    }

    public List<BusinessProfileValidation> getBusinessProfileValidationsByProfileId(
            String profileId, Integer limit
    ){
        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();
        businessProfileValidation.setProfileId(profileId);
        DynamoDBQueryExpression<BusinessProfileValidation> queryExpression = new DynamoDBQueryExpression<BusinessProfileValidation>()
                .withIndexName("profileId-validationRequestEventTime-index") // GSI name
                .withConsistentRead(false) // Set to true for strongly consistent reads
                .withHashKeyValues(businessProfileValidation)// Use hash key value
                .withRangeKeyCondition("validationRequestEventTime", new Condition()
                        .withAttributeValueList(new AttributeValue().withN(Long.toString(Instant.now().toEpochMilli())))
                        .withComparisonOperator(ComparisonOperator.LE))
                .withScanIndexForward(false) // Scanning in reverse
                .withLimit(limit);
        return dynamoDBMapper.queryPage(BusinessProfileValidation.class, queryExpression).getResults();

    }
}
