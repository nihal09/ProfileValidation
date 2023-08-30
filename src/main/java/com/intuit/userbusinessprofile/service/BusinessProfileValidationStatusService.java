package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileValidationResultDto;
import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.repository.BusinessProfileValidationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessProfileValidationStatusService {

    private final ModelMapper modelMapper;
    private final BusinessProfileValidationRepository businessProfileValidationRepository;

    public BusinessProfileValidationStatusService(ModelMapper modelMapper, BusinessProfileValidationRepository businessProfileValidationRepository) {
        this.modelMapper = modelMapper;
        this.businessProfileValidationRepository = businessProfileValidationRepository;
    }


    public List<BusinessProfileValidationResultDto> getBusinessProfileValidationsByProfileId(String profileId, Integer limit) {
        List<BusinessProfileValidation> businessProfileValidations = businessProfileValidationRepository.getBusinessProfileValidationsByProfileId(profileId, limit);
        if(businessProfileValidations.isEmpty()){
            throw new EntityNotFoundException("No validation request found for Profile Id - "+ profileId);
        }
        return businessProfileValidations.stream().map(
                        validation -> modelMapper.map(validation, BusinessProfileValidationResultDto.class))
                .toList();
    }

    @Cacheable(value = "LatestValidationResultProfileId",key = "#profileId")
    public BusinessProfileValidationResultDto getLatestBusinessProfileValidationByProfileId(String profileId) {
        System.out.println("jbfbjkbhbkjjk---------\nbjbbh");
        return getLatestBusinessProfileValidationByProfileIdFromDb(profileId);
    }

    @CachePut(value = "LatestValidationResultProfileId",key = "#profileId")
    public BusinessProfileValidationResultDto getLatestBusinessProfileValidationByProfileIdUpdateCache(String profileId) {
        System.out.println("latest------------------\n-------------------");
        return getLatestBusinessProfileValidationByProfileIdFromDb(profileId);
    }
    public BusinessProfileValidationResultDto getLatestBusinessProfileValidationByProfileIdFromDb(String profileId) {
        List<BusinessProfileValidation> businessProfileValidations = businessProfileValidationRepository.getBusinessProfileValidationsByProfileId(profileId, 1);
        if(businessProfileValidations.isEmpty()){
            throw new EntityNotFoundException("No validation request found for Profile Id - "+ profileId);
        }
        return businessProfileValidations.stream().map(
                        validation -> modelMapper.map(validation, BusinessProfileValidationResultDto.class))
                .toList().get(0);
    }

    @CachePut(value = "BusinessProfileValidation",key = "#validationId")
    public BusinessProfileValidation getBusinessProfileValidationAndUpdateCache(String validationId){
        System.out.println("89976------------------\n-------------------");
        return getBusinessProfileValidationFromDb(validationId);
    }

    @Cacheable(value = "BusinessProfileValidation",key = "#validationId")
    public BusinessProfileValidationResultDto getBusinessProfileValidationStatus(String validationId) {
        return modelMapper.map(getBusinessProfileValidationFromDb(validationId),BusinessProfileValidationResultDto.class);
    }
    public BusinessProfileValidation getBusinessProfileValidationFromDb(String validationId){
        BusinessProfileValidation businessProfileValidation = businessProfileValidationRepository.getBusinessProfileValidation(validationId);
        if(businessProfileValidation == null){
            throw new EntityNotFoundException("Validation task not found for id - "+validationId);
        }
        return businessProfileValidation;
    }
}
