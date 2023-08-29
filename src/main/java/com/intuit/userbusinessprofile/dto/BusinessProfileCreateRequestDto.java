package com.intuit.userbusinessprofile.dto;

import com.intuit.userbusinessprofile.model.Address;
import com.intuit.userbusinessprofile.model.TaxIdentifiers;
import lombok.Data;

@Data
public class BusinessProfileCreateRequestDto {

    private String userId;

    private String companyName;

    private String legalName;

    private Address businessAddress;

    private Address legalAddress;

    private TaxIdentifiers taxIdentifiers;

    private String email;

    private String website;
}
