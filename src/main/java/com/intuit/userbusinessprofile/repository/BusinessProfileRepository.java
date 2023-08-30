package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import org.springframework.stereotype.Repository;

@Repository
public class BusinessProfileRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public BusinessProfileRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public BusinessProfile getBusinessProfile(String profileId) {
        return dynamoDBMapper.load(BusinessProfile.class,profileId);
    }

}

