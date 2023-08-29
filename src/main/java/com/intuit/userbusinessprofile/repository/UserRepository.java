package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.intuit.userbusinessprofile.model.User;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final DynamoDBMapper dynamoDBMapper;

    public UserRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Cacheable(key = "#userId", value = "Users")
    public User getUser(String userId){
        return dynamoDBMapper.load(User.class,userId);
    }
    @CachePut(key = "#userId", value = "Users")
    public User getUserAndUpdateCache(String userId){
        System.out.println("90876578-----------------\n-------------\n----------");
        return dynamoDBMapper.load(User.class,userId);
    }

    public User createUser(User user){
        dynamoDBMapper.save(user);
        return user;
    }

    public void test(User user){
        TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
        transactionWriteRequest.addUpdate(user);
        dynamoDBMapper.transactionWrite(transactionWriteRequest);
    }
}
