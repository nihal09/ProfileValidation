package com.intuit.userbusinessprofile.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import org.springframework.lang.Nullable;
import lombok.Data;

import java.io.Serializable;

@DynamoDBTable(tableName = "BusinessProfileValidation")
@Data
public class BusinessProfileValidation implements Serializable {
    @DynamoDBHashKey(attributeName = "validationId")
    private String validationId;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "profileId-validationRequestEventTime-index", attributeName = "validationRequestEventTime")
    private Long validationRequestEventTime;

    @DynamoDBTypeConvertedEnum
    private Status status;

    @Nullable
    private String rejectionReason;

    @Nullable
    private String failureReason;

    @Nullable
    @DynamoDBTypeConvertedEnum
    private TerminationReason terminationReason;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "profileId-validationRequestEventTime-index", attributeName = "profileId")
    private String profileId;

    private String companyName;

    private String legalName;

    private Address businessAddress;

    private Address legalAddress;

    private TaxIdentifiers taxIdentifiers;

    private String email;

    private String website;

    private Long createdAt;

    private Long updatedAt;
}
