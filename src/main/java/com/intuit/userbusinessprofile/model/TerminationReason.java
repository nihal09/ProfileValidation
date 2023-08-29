package com.intuit.userbusinessprofile.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;

@DynamoDBDocument
public enum TerminationReason {
    PROFILE_ALREADY_UPDATED_WITH_NEW_REQUEST
}
