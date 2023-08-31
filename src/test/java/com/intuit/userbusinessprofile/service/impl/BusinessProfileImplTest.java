package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.model.BusinessProfile;
import com.intuit.userbusinessprofile.repository.BusinessProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class BusinessProfileImplTest {
    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @InjectMocks
    private BusinessProfileImpl businessProfileService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetBusinessProfile_Cacheable() {
        String profileId = "123";
        BusinessProfile businessProfile = new BusinessProfile();
        when(businessProfileRepository.getBusinessProfile(profileId))
                .thenReturn(businessProfile);

        BusinessProfile result = businessProfileService.getBusinessProfile(profileId);

        assertNotNull(result);
    }

    @Test
    public void testGetBusinessProfileAndUpdateCache() {
        String profileId = "123";
        BusinessProfile businessProfile = new BusinessProfile();
        when(businessProfileRepository.getBusinessProfile(profileId))
                .thenReturn(businessProfile);

        BusinessProfile result = businessProfileService.getBusinessProfileAndUpdateCache(profileId);

        assertNotNull(result);

    }
}