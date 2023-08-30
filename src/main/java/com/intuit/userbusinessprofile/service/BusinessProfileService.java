package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.model.BusinessProfile;

public interface BusinessProfileService {
    BusinessProfile getBusinessProfile(String profileId);

    BusinessProfile getBusinessProfileAndUpdateCache(String profileId);
}
