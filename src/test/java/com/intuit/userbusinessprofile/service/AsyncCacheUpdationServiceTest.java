package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileValidationResultDto;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class AsyncCacheUpdationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private BusinessProfileService businessProfileService;

    @Mock
    private BusinessProfileValidationStatusService businessProfileValidationService;

    @InjectMocks
    private AsyncCacheUpdationService asyncCacheUpdationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testUpdateUserCache_Success() {
        String userId = "user123";

        User user = new User();
        when(userService.getUserAndUpdateCache(userId)).thenReturn(user);

        asyncCacheUpdationService.updateUserCache(userId);

        // Verify the behavior
        verify(userService).getUserAndUpdateCache(userId);
    }

    @Test
    public void testUpdateUserCache_Exception() {
        String userId = "user123";

        when(userService.getUserAndUpdateCache(userId)).thenThrow(new RuntimeException("Test exception"));

        asyncCacheUpdationService.updateUserCache(userId);

        // Verify the behavior
        verify(userService).getUserAndUpdateCache(userId);
    }

    @Test
    public void testUpdateBusinessProfileCache_Success() {
        String profileId = "profile123";

        BusinessProfile businessProfile = new BusinessProfile();
        when(businessProfileService.getBusinessProfileAndUpdateCache(profileId)).thenReturn(businessProfile);

        asyncCacheUpdationService.updateBusinessProfileCache(profileId);

        // Verify the behavior
        verify(businessProfileService).getBusinessProfileAndUpdateCache(profileId);
    }

    @Test
    public void testUpdateBusinessProfileCache_Exception() {
        String profileId = "profile123";

        when(businessProfileService.getBusinessProfileAndUpdateCache(profileId)).thenThrow(new RuntimeException("Test exception"));

        asyncCacheUpdationService.updateBusinessProfileCache(profileId);

        // Verify the behavior
        verify(businessProfileService).getBusinessProfileAndUpdateCache(profileId);
    }

    @Test
    public void testUpdateLatestValidationByProfileIdCache_Success() {
        String profileId = "profile123";

        BusinessProfileValidationResultDto validationResult = new BusinessProfileValidationResultDto();
        when(businessProfileValidationService.getLatestBusinessProfileValidationByProfileIdUpdateCache(profileId)).thenReturn(validationResult);

        asyncCacheUpdationService.updateLatestValidationByProfileIdCache(profileId);

        // Verify the behavior
        verify(businessProfileValidationService).getLatestBusinessProfileValidationByProfileIdUpdateCache(profileId);
    }

    @Test
    public void testUpdateLatestValidationByProfileIdCache_Exception() {
        String profileId = "profile123";

        when(businessProfileValidationService.getLatestBusinessProfileValidationByProfileIdUpdateCache(profileId)).thenThrow(new RuntimeException("Test exception"));

        asyncCacheUpdationService.updateLatestValidationByProfileIdCache(profileId);

        // Verify the behavior
        verify(businessProfileValidationService).getLatestBusinessProfileValidationByProfileIdUpdateCache(profileId);
    }

    @Test
    public void testUpdateBusinessProfileValidationCache_Success() {
        String validationId = "validation123";

        BusinessProfileValidation validation = new BusinessProfileValidation();
        when(businessProfileValidationService.getBusinessProfileValidationAndUpdateCache(validationId)).thenReturn(validation);

        asyncCacheUpdationService.updateBusinessProfileValidationCache(validationId);

        // Verify the behavior
        verify(businessProfileValidationService).getBusinessProfileValidationAndUpdateCache(validationId);
    }

    @Test
    public void testUpdateBusinessProfileValidationCache_Exception() {
        String validationId = "validation123";

        when(businessProfileValidationService.getBusinessProfileValidationAndUpdateCache(validationId)).thenThrow(new RuntimeException("Test exception"));

        asyncCacheUpdationService.updateBusinessProfileValidationCache(validationId);

        // Verify the behavior
        verify(businessProfileValidationService).getBusinessProfileValidationAndUpdateCache(validationId);
    }

}
