package com.intuit.userbusinessprofile.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.model.Status;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BusinessProfileValidationRepositoryTest {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private BusinessProfileValidationRepository repository;

    private BusinessProfileValidation validation = new BusinessProfileValidation();

    @Test
    public void testCreateBusinessProfileValidationTask() {
        validation.setValidationId("validation-1");
        validation.setProfileId("profile-1");
        validation.setStatus(Status.ACCEPTED);

        BusinessProfileValidation result = repository.createBusinessProfileValidationTask(validation);
        assertNotNull(result);
        assertEquals("validation-1", result.getValidationId());
    }
}
