package com.intuit.userbusinessprofile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncCacheUpdationService {
    private final UserService userService;
    private final BusinessProfileService businessProfileService;

    private final BusinessProfileValidationStatusService businessProfileValidationService;
    private static final Logger logger = LoggerFactory.getLogger(AsyncCacheUpdationService.class);
    public AsyncCacheUpdationService(UserService userService, BusinessProfileService businessProfileService, BusinessProfileValidationStatusService businessProfileValidationStatusService) {
        this.userService = userService;
        this.businessProfileService = businessProfileService;
        this.businessProfileValidationService = businessProfileValidationStatusService;
    }

    @Async
    public void updateUserCache(String userId) {
        try {
            userService.getUserAndUpdateCache(userId);
        } catch (Exception e){
            logger.error("Failed to update UserCache");
        }
    }

    @Async
    public void updateBusinessProfileCache(String profileId) {
        try {
            businessProfileService.getBusinessProfileAndUpdateCache(profileId);
        } catch (Exception e){
            logger.error("Failed to update BusinessProfileCache");
        }
    }

    @Async
    public void updateLatestValidationByProfileIdCache(String profileId) {
        try {
            businessProfileValidationService.getLatestBusinessProfileValidationByProfileIdUpdateCache(profileId);
        } catch (Exception e){
            logger.error("Failed to update LatestBusinessProfileValidation cache");
        }
    }

    @Async
    public void updateBusinessProfileValidationCache(String validationId){
        try {
            businessProfileValidationService.getBusinessProfileValidationAndUpdateCache(validationId);
        } catch (Exception e){
            logger.error("Failed to update BusinessProfileValidation cache");
        }
    }

}
