package com.intuit.userbusinessprofile.dto;

import com.intuit.userbusinessprofile.dto.enums.BusinessProfileTaskType;
import com.intuit.userbusinessprofile.model.Address;
import com.intuit.userbusinessprofile.model.TaxIdentifiers;
import lombok.Data;

@Data
public class BusinessProfileCreateUpdateValidationRequestDto {

    private String userId;

    private String validationId;

    private String profileId;

    private String companyName;

    private String legalName;

    private Address businessAddress;

    private Address legalAddress;

    private TaxIdentifiers taxIdentifiers;

    private String email;

    private String website;

    private Long validationRequestEventTime;

    private BusinessProfileTaskType businessProfileTaskType;

}
