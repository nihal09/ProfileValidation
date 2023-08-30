package com.intuit.userbusinessprofile.controller;

import com.intuit.userbusinessprofile.dto.*;
import com.intuit.userbusinessprofile.dto.enums.BusinessProfileTaskType;
import com.intuit.userbusinessprofile.model.*;
import com.intuit.userbusinessprofile.producer.ProfileValidationKafkaProducer;
import com.intuit.userbusinessprofile.service.BusinessProfileService;
import com.intuit.userbusinessprofile.service.BusinessProfileValidationService;
import com.intuit.userbusinessprofile.service.BusinessProfileValidationStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/business-profiles")
@CrossOrigin("*")
public class BusinessProfileController {

    private final BusinessProfileService businessProfileService;

    private final BusinessProfileValidationService businessProfileValidationService;

    private final BusinessProfileValidationStatusService businessProfileValidationStatusService;
    private final ProfileValidationKafkaProducer profileValidationKafkaProducer;

    @Autowired
    public BusinessProfileController(BusinessProfileService businessProfileService, BusinessProfileValidationService businessProfileValidationService, BusinessProfileValidationStatusService businessProfileValidationStatusService, ProfileValidationKafkaProducer profileValidationKafkaProducer) {
        this.businessProfileService = businessProfileService;
        this.businessProfileValidationService = businessProfileValidationService;
        this.businessProfileValidationStatusService = businessProfileValidationStatusService;
        this.profileValidationKafkaProducer = profileValidationKafkaProducer;
    }

    @PostMapping("/test")
    public ResponseEntity<BusinessProfileCreateUpdateValidationRequestDto> test() {
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

    @PostMapping("create-business-profile")
    public ResponseEntity<BusinessProfileCreateUpdateRequestResponseDto> validateAndCreateBusinessProfile(@RequestBody BusinessProfileValidateAndCreateRequestDto requestDto) {
        String validationId = UUID.randomUUID().toString();
        businessProfileValidationService.initiateValidationForBusinessProfileCreation(requestDto, validationId);
        BusinessProfileCreateUpdateRequestResponseDto responseDto = new BusinessProfileCreateUpdateRequestResponseDto();
        responseDto.setValidationId(validationId);
        responseDto.setResult("Accepted");
        return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
    }

    @PutMapping("update-business-profile")
    public ResponseEntity<BusinessProfileCreateUpdateRequestResponseDto> validateAndUpdateBusinessProfile(@RequestBody BusinessProfileValidateAndUpdateRequestDto requestDto) {
        String validationId = UUID.randomUUID().toString();
        businessProfileValidationService.initiateValidationForBusinessProfileUpdation(requestDto, validationId);
        BusinessProfileCreateUpdateRequestResponseDto responseDto = new BusinessProfileCreateUpdateRequestResponseDto();
        responseDto.setValidationId(validationId);
        responseDto.setResult("Accepted");
        return new ResponseEntity<>(responseDto, HttpStatus.ACCEPTED);
    }

    @GetMapping("fetch-latest-business-profile-update-request-status/profileId/{profileId}")
    public ResponseEntity<BusinessProfileValidationResultDto> getLatestBusinessProfileValidationByProfileId(@PathVariable String profileId) {
        return ResponseEntity.ok(businessProfileValidationStatusService.getLatestBusinessProfileValidationByProfileId(profileId));
    }

    @GetMapping("fetch-business-profile-update-request-status/profileId/{profileId}")
    public ResponseEntity<List<BusinessProfileValidationResultDto>> getBusinessProfileValidationsByProfileId(@RequestParam Integer limit, @PathVariable String profileId) {
        return ResponseEntity.ok(businessProfileValidationStatusService.getBusinessProfileValidationsByProfileId(profileId, limit));
    }

    @GetMapping("fetch-business-profile-update-request-status/validationId/{validationId}")
    public ResponseEntity<BusinessProfileValidationResultDto> getBusinessProfileValidationByValidationId(@PathVariable String validationId) {
        return ResponseEntity.ok(businessProfileValidationStatusService.getBusinessProfileValidationStatus(validationId));
    }

    @GetMapping("get-business-profile/{profileId}")
    public ResponseEntity<BusinessProfile> getBusinessProfileByProfileId(@PathVariable String profileId) {
        return ResponseEntity.ok(businessProfileService.getBusinessProfile(profileId));
    }
}
