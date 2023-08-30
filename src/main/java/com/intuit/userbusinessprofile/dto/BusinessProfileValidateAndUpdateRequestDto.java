package com.intuit.userbusinessprofile.dto;

import com.intuit.userbusinessprofile.model.Address;
import com.intuit.userbusinessprofile.model.TaxIdentifiers;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class BusinessProfileValidateAndUpdateRequestDto {

    private String userId;

    @Nullable
    private String profileId;

    private String companyName;

    private String legalName;

    private Address businessAddress;

    private Address legalAddress;

    private TaxIdentifiers taxIdentifiers;

    private String email;

    private String website;

    private Long requestTime;

}
