package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.Before;


@SpringBootTest
class UserRepositoryTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;
    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void createUser() {

    }
}