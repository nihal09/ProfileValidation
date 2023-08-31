package com.intuit.userbusinessprofile.model;


import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
    }

    @Test
    void testSerializeToDynamoDBItem() {
        String userId = "user-123";
        String firstName = "John";
        String lastName = "Doe";
        String businessProfileId = "business-456";
        Set<Product> subscribedProducts = Set.of(Product.QB, Product.T_SHEETS);

        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setBusinessProfileId(businessProfileId);
        user.setSubscribedProducts(subscribedProducts);

        Map<String, AttributeValue> expectedItem = new HashMap<>();
        expectedItem.put("userId", new AttributeValue().withS(userId));
        expectedItem.put("firstName", new AttributeValue().withS(firstName));
        expectedItem.put("lastName", new AttributeValue().withS(lastName));
        expectedItem.put("businessProfileId", new AttributeValue().withS(businessProfileId));

        when(dynamoDBMapper.marshallIntoObject(User.class, expectedItem))
                .thenReturn(user);

        User serializedUser = dynamoDBMapper.marshallIntoObject(User.class, expectedItem);

        assertEquals(user, serializedUser);
    }
}
