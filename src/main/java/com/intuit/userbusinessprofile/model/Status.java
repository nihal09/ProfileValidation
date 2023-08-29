package com.intuit.userbusinessprofile.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

public enum Status {
    IN_PROGRESS,
    ACCEPTED,
    REJECTED,
    FAILED,
    TERMINATED; // if a new profile updation request comes
    Status(){

    }
}
