package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.dto.BusinessProfileResponseDto;
import com.intuit.userbusinessprofile.model.BusinessProfile;
import com.intuit.userbusinessprofile.repository.BusinessProfileRepository;
import com.intuit.userbusinessprofile.service.BusinessProfileService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessProfileImpl implements BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;

    private final ModelMapper modelMapper;

    @Autowired
    BusinessProfileImpl(BusinessProfileRepository businessProfileRepository,ModelMapper modelMapper){
        this.businessProfileRepository = businessProfileRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public BusinessProfileResponseDto createBusinessProfile(BusinessProfile businessProfile) {
        businessProfileRepository.createBusinessProfile(businessProfile);
        return modelMapper.map(businessProfile, BusinessProfileResponseDto.class);
    }

    @Override
    public BusinessProfileResponseDto updateBusinessProfile(BusinessProfile businessProfile) {
        businessProfileRepository.updateBusinessProfile(businessProfile);
        return modelMapper.map(businessProfile, BusinessProfileResponseDto.class);
    }

    @Override
    public BusinessProfile getBusinessProfile(String profileId) {
        return businessProfileRepository.getBusinessProfile(profileId);
    }
}
