package com.intuit.userbusinessprofile.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import com.intuit.userbusinessprofile.model.BusinessProfileHistory;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TransactionServiceTest {

    @Mock
    private DynamoDBMapper dynamoDBMapper;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        transactionService = new TransactionService(dynamoDBMapper);
    }

    @Test
    void testCreateProfileUpdateUserAndValidationTaskInTransaction() {
        User updatedUser = new User();
        BusinessProfile newBusinessProfile = new BusinessProfile();
        BusinessProfileValidation updatedBusinessProfileValidation = new BusinessProfileValidation();

        transactionService.createProfileUpdateUserAndValidationTaskInTransaction(updatedUser, newBusinessProfile, updatedBusinessProfileValidation);

        verify(dynamoDBMapper, times(1)).transactionWrite(any());
    }

    @Test
    void testUpdateProfileAndTaskCreateProfileHistoryInTransaction() {
        BusinessProfileHistory businessProfileHistory = new BusinessProfileHistory();
        BusinessProfile updatedBusinessProfile = new BusinessProfile();
        BusinessProfileValidation updatedBusinessProfileValidation = new BusinessProfileValidation();

        transactionService.updateProfileAndTaskCreateProfileHistoryInTransaction(businessProfileHistory, updatedBusinessProfile, updatedBusinessProfileValidation);

        verify(dynamoDBMapper, times(1)).transactionWrite(any());
    }
}