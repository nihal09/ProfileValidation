package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import com.intuit.userbusinessprofile.repository.BusinessProfileRepository;
import com.intuit.userbusinessprofile.service.BusinessProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BusinessProfileImpl implements BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;

    @Autowired
    BusinessProfileImpl(BusinessProfileRepository businessProfileRepository){
        this.businessProfileRepository = businessProfileRepository;
    }
/*
    @Override
    public BusinessProfileResponseDto createBusinessProfile(BusinessProfile businessProfile) {
        businessProfileRepository.createBusinessProfile(businessProfile);
        return modelMapper.map(businessProfile, BusinessProfileResponseDto.class);
    }
*/

    @Override
    @Cacheable(value = "BusinessProfile",key = "#profileId")
    public BusinessProfile getBusinessProfile(String profileId) {
        return getBusinessProfileFromDb(profileId);
    }
    @Override
    @CachePut(value = "BusinessProfile",key = "#profileId")
    public BusinessProfile getBusinessProfileAndUpdateCache(String profileId) {
        return getBusinessProfileFromDb(profileId);
    }

    private BusinessProfile getBusinessProfileFromDb(String profileId) {
        BusinessProfile businessProfile = businessProfileRepository.getBusinessProfile(profileId);
        if(businessProfile == null)
            throw new EntityNotFoundException("BusinessProfile not found with id - "+profileId);
        return businessProfile;
    }

}
