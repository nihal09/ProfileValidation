package com.intuit.userbusinessprofile.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@DynamoDBDocument
public class TaxIdentifiers implements Serializable {
    private String pan;
    private String ein;
}
