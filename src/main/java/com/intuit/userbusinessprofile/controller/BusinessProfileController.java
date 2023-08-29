package com.intuit.userbusinessprofile.controller;

import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.dto.BusinessProfileResponseDto;
import com.intuit.userbusinessprofile.dto.enums.BusinessProfileTaskType;
import com.intuit.userbusinessprofile.model.*;
import com.intuit.userbusinessprofile.producer.ProfileValidationKafkaProducer;
import com.intuit.userbusinessprofile.service.BusinessProfileService;
import com.intuit.userbusinessprofile.service.BusinessProfileValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/business-profiles")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    private final BusinessProfileValidationService businessProfileValidationService;
    private final ProfileValidationKafkaProducer profileValidationKafkaProducer;

    @Autowired
    public BusinessProfileController(BusinessProfileService businessProfileService , BusinessProfileValidationService businessProfileValidationService, ProfileValidationKafkaProducer profileValidationKafkaProducer) {
        this.businessProfileService = businessProfileService;
        this.businessProfileValidationService = businessProfileValidationService;
        this.profileValidationKafkaProducer = profileValidationKafkaProducer;
    }

    @PostMapping
    public ResponseEntity<BusinessProfileResponseDto> createBusinessProfile(@RequestBody BusinessProfile businessProfile) {
        BusinessProfileResponseDto createdProfile = businessProfileService.createBusinessProfile(businessProfile);
        return ResponseEntity.ok(createdProfile);
    }

    @PostMapping("/test")
    public ResponseEntity<BusinessProfileCreateUpdateValidationRequestDto> test() throws ExecutionException, InterruptedException {
        //User test = businessProfileValidationService.test("8787");
        BusinessProfileCreateUpdateValidationRequestDto request = new BusinessProfileCreateUpdateValidationRequestDto();
        Address address = new Address();
        TaxIdentifiers taxIdentifiers = new TaxIdentifiers();
        taxIdentifiers.setEin("nk");
        taxIdentifiers.setPan("hjg");
        address.setAddressLine1("w");
        address.setAddressLine2("s");
        address.setCity("d");
        address.setCountry("Paris");
        address.setZip("dd");
        address.setState("dd");
        request.setProfileId("e87f8875-158f-4383-ac90-4ab70230ec9a");
        request.setUserId("6a291a13-3a3f-4cf1-b51d-85e0fa8d8166");
        request.setBusinessAddress(address);
        request.setLegalAddress(address);
        request.setEmail("dd");
        request.setBusinessProfileTaskType(BusinessProfileTaskType.UPDATE);
        request.setWebsite("dff");
        request.setLegalName("fff");
        request.setCompanyName("ffff");
        request.setTaxIdentifiers(taxIdentifiers);
        request.setValidationId(UUID.randomUUID().toString());
        request.setValidationRequestEventTime(453453449l);
        //businessProfileValidationService.validateAndUpdateBusinessProfileIfRequired(request);
        profileValidationKafkaProducer.sendMessage(request);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/test")
    public ResponseEntity<BusinessProfile> test1(){
        return ResponseEntity.ok(businessProfileService.getBusinessProfile("d1c97677-0a94-4bfb-84ae-2a3ebcbfcf7e"));
    }

    @GetMapping("/test1")
    public ResponseEntity<BusinessProfileValidation> test2(){
        return ResponseEntity.ok(businessProfileValidationService.getBusinessProfileValidation("d1c97677-0a94-4bfb-84ae-2a3ebcbfcf7e"));
    }
}
