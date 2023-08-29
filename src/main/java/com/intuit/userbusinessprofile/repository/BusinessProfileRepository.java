package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class BusinessProfileRepository {

    private final DynamoDBMapper dynamoDBMapper;


    public BusinessProfileRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public BusinessProfile createBusinessProfile(BusinessProfile businessProfile) {
        dynamoDBMapper.save(businessProfile);
        return businessProfile;
    }

    public void updateBusinessProfile(BusinessProfile businessProfile) {
        businessProfile.setUpdatedAt(Instant.now().toEpochMilli());
        dynamoDBMapper.save(businessProfile);
    }

    @Cacheable(value = "BusinessProfile",key = "#profileId")
    public BusinessProfile getBusinessProfile(String profileId) {
        return dynamoDBMapper.load(BusinessProfile.class,profileId);
    }

    @CachePut(value = "BusinessProfile",key = "#profileId")
    public BusinessProfile getBusinessProfileAndUpdateCache(String profileId) {
        System.out.println("BusinessProfile-----------------\n-------------\n----------");
        return dynamoDBMapper.load(BusinessProfile.class,profileId);
    }

}

