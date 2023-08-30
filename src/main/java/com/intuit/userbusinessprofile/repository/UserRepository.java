package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.intuit.userbusinessprofile.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final DynamoDBMapper dynamoDBMapper;

    public UserRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public User getUser(String userId){
        return dynamoDBMapper.load(User.class,userId);
    }

    public User createUser(User user){
        dynamoDBMapper.save(user);
        return user;
    }

}
