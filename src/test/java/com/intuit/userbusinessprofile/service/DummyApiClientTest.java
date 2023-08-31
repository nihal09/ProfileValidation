package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.dto.ProductValidationResultDto;
import com.intuit.userbusinessprofile.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DummyApiClientTest {
    @Test
    void testValidateProfileForAProduct_ValidProduct() {
        DummyApiClient dummyApiClient = new DummyApiClient();
        BusinessProfileCreateUpdateValidationRequestDto validationRequest = new BusinessProfileCreateUpdateValidationRequestDto();
        Product product = Product.QB; // Assuming QB product for this test

        ProductValidationResultDto validationResult = dummyApiClient.validateProfileForAProduct(validationRequest, product);

        // Assert
        assertTrue(validationResult.getIsValid());
        assertEquals(validationResult.getRejectionReason(),"Random reason");
        assertEquals(product, validationResult.getProduct());
    }
}