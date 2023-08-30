package com.intuit.userbusinessprofile.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;

import java.io.Serializable;

@DynamoDBTable(tableName = "BusinessProfileHistory")
@Data
public class BusinessProfileHistory implements Serializable {
    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "profileId-expiredOn-index", attributeName = "profileId")
    private String profileId;

    private String companyName;

    private String legalName;

    private Address businessAddress;

    private Address legalAddress;

    private TaxIdentifiers taxIdentifiers;

    private String email;

    private String website;

    private Long startedOn;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "profileId-expiredOn-index", attributeName = "expiredOn")
    private Long expiredOn;

}
