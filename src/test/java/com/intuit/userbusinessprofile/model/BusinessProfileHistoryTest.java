package com.intuit.userbusinessprofile.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


class BusinessProfileHistoryTest {
    @Test
    public void testSerializationDeserialization() throws Exception {

        BusinessProfileHistory businessProfileHistory = new BusinessProfileHistory();
        businessProfileHistory.setId("123");
        businessProfileHistory.setProfileId("456");
        businessProfileHistory.setCompanyName("Company");
        businessProfileHistory.setLegalName("Legal Company");
        businessProfileHistory.setBusinessAddress(new Address());
        businessProfileHistory.setLegalAddress(new Address());
        businessProfileHistory.setTaxIdentifiers(new TaxIdentifiers());
        businessProfileHistory.setEmail("test@example.com");
        businessProfileHistory.setWebsite("example.com");
        businessProfileHistory.setStartedOn(System.currentTimeMillis());
        businessProfileHistory.setExpiredOn(System.currentTimeMillis() + 1000);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(businessProfileHistory);
        objectOutputStream.close();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        BusinessProfileHistory deserializedProfileHistory = (BusinessProfileHistory) objectInputStream.readObject();

        // Assert that the original and deserialized objects match
        Assertions.assertEquals(businessProfileHistory.getId(), deserializedProfileHistory.getId());
        Assertions.assertEquals(businessProfileHistory.getProfileId(), deserializedProfileHistory.getProfileId());
        Assertions.assertEquals(businessProfileHistory.getCompanyName(), deserializedProfileHistory.getCompanyName());
        Assertions.assertEquals(businessProfileHistory.getLegalName(), deserializedProfileHistory.getLegalName());

        Assertions.assertEquals(businessProfileHistory, deserializedProfileHistory);

        objectOutputStream.close();
        objectInputStream.close();
    }
}