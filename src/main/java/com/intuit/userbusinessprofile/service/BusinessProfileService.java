package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileResponseDto;
import com.intuit.userbusinessprofile.model.BusinessProfile;

public interface BusinessProfileService {

    BusinessProfileResponseDto createBusinessProfile(BusinessProfile businessProfile);
    BusinessProfileResponseDto updateBusinessProfile(BusinessProfile businessProfile);
    BusinessProfile getBusinessProfile(String profileId);


}
