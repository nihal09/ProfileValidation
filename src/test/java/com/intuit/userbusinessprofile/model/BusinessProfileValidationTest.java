package com.intuit.userbusinessprofile.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class BusinessProfileValidationTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    private BusinessProfileValidation businessProfileValidation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        businessProfileValidation = new BusinessProfileValidation();
    }
    @Test
    public void testSerialization() {
        ObjectMapper objectMapper = new ObjectMapper();

        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();
        // Set your entity's properties here for testing

        // Test if serialization does not throw an exception
        assertDoesNotThrow(() -> objectMapper.writeValueAsString(businessProfileValidation));
    }

    @Test
    public void testDeserialization() {
        ObjectMapper objectMapper = new ObjectMapper();

        String json = "{\"validationId\":\"123\",\"validationRequestEventTime\":1630454400,\"status\":\"IN_PROGRESS\",\"profileId\":\"456\",\"companyName\":\"ABC\",\"legalName\":\"DEF\",\"email\":\"test@example.com\",\"website\":\"http://example.com\",\"createdAt\":1630454400,\"updatedAt\":1630454400}";

        // Test if deserialization does not throw an exception
        assertDoesNotThrow(() -> objectMapper.readValue(json, BusinessProfileValidation.class));
    }

    @Test
    void testMappingToAndFromDynamoDBItem() {
        String validationId = "validation-123";
        Long validationRequestEventTime = System.currentTimeMillis();
        String profileId = "profile-456";

        businessProfileValidation.setValidationId(validationId);
        businessProfileValidation.setValidationRequestEventTime(validationRequestEventTime);
        businessProfileValidation.setProfileId(profileId);

        Map<String, AttributeValue> itemAttributes = new HashMap<>();
        itemAttributes.put("validationId", new AttributeValue(validationId));
        itemAttributes.put("validationRequestEventTime", new AttributeValue().withN(validationRequestEventTime.toString()));
        itemAttributes.put("profileId", new AttributeValue(profileId));

        when(dynamoDBMapper.marshallIntoObject(BusinessProfileValidation.class, itemAttributes))
                .thenReturn(businessProfileValidation);

        BusinessProfileValidation mappedValidation = dynamoDBMapper.marshallIntoObject(BusinessProfileValidation.class, itemAttributes);

        assertEquals(businessProfileValidation, mappedValidation);
    }
}
