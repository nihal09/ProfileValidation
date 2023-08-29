package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import com.intuit.userbusinessprofile.model.BusinessProfileHistory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class BusinessProfileHistoryRepository {

    private final DynamoDBMapper dynamoDBMapper;


    public BusinessProfileHistoryRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public BusinessProfileHistory createBusinessProfileHistory(BusinessProfileHistory businessProfile) {
        dynamoDBMapper.save(businessProfile);
        return businessProfile;
    }

    public List<BusinessProfileHistory> getBusinessProfileHistory(String profileId) {
        BusinessProfileHistory businessProfileHistory = new BusinessProfileHistory();
        businessProfileHistory.setProfileId(profileId);
        DynamoDBQueryExpression<BusinessProfileHistory> queryExpression = new DynamoDBQueryExpression<BusinessProfileHistory>()
                .withIndexName("ProfileIndex") // Name of the GSI
                .withHashKeyValues(businessProfileHistory)
                .withConsistentRead(false);
        return dynamoDBMapper.query(BusinessProfileHistory.class, queryExpression);
    }

}

