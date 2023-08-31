package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.dto.*;
import com.intuit.userbusinessprofile.exceptions.ArgumentNotValidException;
import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import com.intuit.userbusinessprofile.model.*;
import com.intuit.userbusinessprofile.producer.ProfileValidationKafkaProducer;
import com.intuit.userbusinessprofile.repository.BusinessProfileValidationRepository;
import com.intuit.userbusinessprofile.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.util.Pair;


import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class BusinessProfileValidationServiceImplTest {

    @InjectMocks
    private BusinessProfileValidationServiceImpl businessProfileValidationService;

    @Mock
    private BusinessProfileValidationRepository businessProfileValidationRepository;

    @Mock
    private UserService userService;

    @Mock
    private DummyApiClient dummyApiClient;

    @Mock
    private TransactionService transactionService;
    @Mock
    private ProfileValidationKafkaProducer profileValidationKafkaProducer;

    @Mock
    private AsyncCacheUpdationService asyncCacheUpdationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testGetBusinessProfileValidationWithoutNullCheck() {
        String validationId = "123";

        BusinessProfileValidation expectedValidation = new BusinessProfileValidation();
        when(businessProfileValidationRepository.getBusinessProfileValidation(anyString()))
                .thenReturn(expectedValidation);

        // Call the method under test
        BusinessProfileValidation resultValidation = businessProfileValidationService.getBusinessProfileValidationWithoutNullCheck(validationId);

        // Verify the interaction and result
        verify(businessProfileValidationRepository).getBusinessProfileValidation(validationId);
        assertEquals(expectedValidation, resultValidation);
    }

    @Test
    public void testInitiateValidationForBusinessProfileCreation() {
        // Prepare test data
        BusinessProfileValidateAndCreateRequestDto requestDto = new BusinessProfileValidateAndCreateRequestDto();
        String validationId = "123";

        // Call the method under test
        businessProfileValidationService.initiateValidationForBusinessProfileCreation(requestDto, validationId);

        // Verify the interactions
        verify(businessProfileValidationRepository, times(1)).createBusinessProfileValidationTask(any());
        verify(profileValidationKafkaProducer, times(1)).sendMessage(any());

        // Verify that other methods were not called
        verify(businessProfileValidationRepository, never()).getBusinessProfileValidation(anyString());
        verify(userService, never()).getUser(anyString());
        verify(dummyApiClient, never()).validateProfileForAProduct(any(), any());
        verify(transactionService, never()).createProfileUpdateUserAndValidationTaskInTransaction(any(), any(), any());

    }


    @Test
    public void testInitiateValidationForBusinessProfileUpdation_NullProfileId() {
        BusinessProfileValidateAndUpdateRequestDto requestDto = new BusinessProfileValidateAndUpdateRequestDto();
        String validationId = "123";

        assertThrows(ArgumentNotValidException.class,
                () -> businessProfileValidationService.initiateValidationForBusinessProfileUpdation(requestDto, validationId));

    }

    @Test
    public void testInitiateValidationForBusinessProfileUpdation() {
        BusinessProfileValidateAndUpdateRequestDto requestDto = new BusinessProfileValidateAndUpdateRequestDto();
        requestDto.setProfileId("profile123");
        String validationId = "123";

        businessProfileValidationService.initiateValidationForBusinessProfileUpdation(requestDto, validationId);

        // Verify the interactions
        verify(businessProfileValidationRepository, times(1)).createBusinessProfileValidationTask(any());
        verify(profileValidationKafkaProducer, times(1)).sendMessage(any());

    }

    @Test
    public void testRejectBusinessProfileValidationTask() {
        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();
        String rejectionReason = "Invalid data";
        businessProfileValidation.setProfileId("test");
        businessProfileValidationService.rejectBusinessProfileValidationTask(businessProfileValidation, rejectionReason);

        // Verify the interactions
        assertEquals(Status.REJECTED, businessProfileValidation.getStatus());
        assertEquals(rejectionReason, businessProfileValidation.getRejectionReason());
        verify(businessProfileValidationRepository, times(1)).updateBusinessProfileValidationTask(any());
        verify(asyncCacheUpdationService, times(1)).updateLatestValidationByProfileIdCache(anyString());
    }

    @Test
    public void testUpdateProfileAndTaskCreateProfileHistoryIfRequired() {
        BusinessProfile businessProfile = new BusinessProfile();
        businessProfile.setLatestApprovedValidationEventTime(1000L); // Some past time
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        requestDto.setValidationRequestEventTime(2000L); // A newer time
        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();

        businessProfileValidationService.updateProfileAndTaskCreateProfileHistoryIfRequired(
                businessProfile, requestDto, businessProfileValidation);

        // Verify interactions
        verify(transactionService, times(1))
                .updateProfileAndTaskCreateProfileHistoryInTransaction(
                        any(), any(), any());

        verify(asyncCacheUpdationService, times(1))
                .updateBusinessProfileCache(businessProfile.getProfileId());

        verify(asyncCacheUpdationService, times(1))
                .updateLatestValidationByProfileIdCache(businessProfile.getProfileId());
    }

    @Test
    public void testUpdateProfileAndTaskCreateProfileHistoryIfRequired_TerminatedStatus() {
        // Prepare test data
        BusinessProfile businessProfile = new BusinessProfile();
        businessProfile.setLatestApprovedValidationEventTime(2000L); // A newer time
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        requestDto.setValidationRequestEventTime(1000L); // An older time
        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();

        businessProfileValidationService.updateProfileAndTaskCreateProfileHistoryIfRequired(
                businessProfile, requestDto, businessProfileValidation);


        verify(asyncCacheUpdationService, times(1))
                .updateLatestValidationByProfileIdCache(businessProfile.getProfileId());

        // Verify that updateBusinessProfileCache was not called
        verify(asyncCacheUpdationService, never())
                .updateBusinessProfileCache(any());
    }

    @Test
    public void testUpdateProfileAndTaskCreateProfileHistoryInTransaction() {
        // Prepare test data
        BusinessProfile businessProfile = new BusinessProfile();
        businessProfile.setProfileId("123");
        businessProfile.setUpdatedAt(1000L); // Some previous time
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        requestDto.setValidationRequestEventTime(2000L); // A newer time
        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();
        businessProfileValidation.setStatus(Status.IN_PROGRESS);

        doNothing().when(transactionService).updateProfileAndTaskCreateProfileHistoryInTransaction(
                any(), any(), any());

        businessProfileValidationService.updateProfileAndTaskCreateProfileHistoryInTransaction(
                businessProfile, requestDto, businessProfileValidation);

        // Verify that the businessProfile and businessProfileValidation were updated correctly
        assertEquals(requestDto.getCompanyName(), businessProfile.getCompanyName());
        assertEquals(requestDto.getLegalName(), businessProfile.getLegalName());
        assertEquals(requestDto.getBusinessAddress(), businessProfile.getBusinessAddress());
        assertEquals(requestDto.getLegalAddress(), businessProfile.getLegalAddress());
        assertEquals(requestDto.getTaxIdentifiers(), businessProfile.getTaxIdentifiers());
        assertEquals(requestDto.getEmail(), businessProfile.getEmail());
        assertEquals(requestDto.getWebsite(), businessProfile.getWebsite());
        assertEquals(requestDto.getValidationRequestEventTime(), businessProfile.getLatestApprovedValidationEventTime());

        assertEquals(Status.ACCEPTED, businessProfileValidation.getStatus());

        // Verify interactions with transactionService
        verify(transactionService, times(1))
                .updateProfileAndTaskCreateProfileHistoryInTransaction(
                        any(BusinessProfileHistory.class), eq(businessProfile), eq(businessProfileValidation));
    }

    @Test
    public void testCreateProfileUpdateUserAndValidationTaskInTransaction() {
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();
        businessProfileValidation.setStatus(Status.IN_PROGRESS);
        User user = new User();
        user.setUserId("456");

        doNothing().when(transactionService).createProfileUpdateUserAndValidationTaskInTransaction(
                any(), any(), any());

        businessProfileValidationService.createProfileUpdateUserAndValidationTaskInTransaction(
                requestDto, businessProfileValidation, user);

        // Verify that user, businessProfileValidation, and businessProfile were updated correctly
        assertNotNull(user.getBusinessProfileId());
        assertNotNull(user.getUpdatedAt());

        assertNotNull(businessProfileValidation.getProfileId());
        assertEquals(Status.ACCEPTED, businessProfileValidation.getStatus());
        assertNotNull(businessProfileValidation.getUpdatedAt());

        // Verify interactions with transactionService
        verify(transactionService, times(1))
                .createProfileUpdateUserAndValidationTaskInTransaction(
                        eq(user), any(BusinessProfile.class), eq(businessProfileValidation));

        // Verify cache update interactions
        verify(asyncCacheUpdationService, times(1))
                .updateUserCache(user.getUserId());
        verify(asyncCacheUpdationService, times(1))
                .updateBusinessProfileCache(businessProfileValidation.getProfileId());
        verify(asyncCacheUpdationService, times(1))
                .updateBusinessProfileValidationCache(businessProfileValidation.getValidationId());
    }

    @Test
    public void testValidateBusinessProfileForSubscribedProducts_AllValid() throws Exception {
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        Set<Product> subscribedProducts = Set.of(Product.QB, Product.QB_PAYMENTS);

        ProductValidationResultDto validProduct =
                new ProductValidationResultDto(true, "Valid product",Product.QB);
        when(dummyApiClient.validateProfileForAProduct(eq(requestDto), any(Product.class)))
                .thenReturn(validProduct);

        ValidationResultDto validationResult = businessProfileValidationService.validateBusinessProfileForSubscribedProducts(
                requestDto, subscribedProducts);

        // Assert that the validation result is valid
        assertTrue(validationResult.getIsValid());
        assertNull(validationResult.getRejectionReason());

        // Verify interactions with dummyApiClient
        verify(dummyApiClient, times(subscribedProducts.size()))
                .validateProfileForAProduct(eq(requestDto), any(Product.class));
    }

    @Test
    public void testValidateBusinessProfileForSubscribedProducts_SomeInvalid() throws Exception {
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        Set<Product> subscribedProducts = Set.of(Product.QB, Product.QB_PAYMENTS);

        ProductValidationResultDto validProduct =
                new ProductValidationResultDto(true, "Valid product",Product.QB);
        ProductValidationResultDto invalidProduct =
                new ProductValidationResultDto(false, "Invalid product",Product.QB_PAYMENTS);
        when(dummyApiClient.validateProfileForAProduct(requestDto, Product.QB))
                .thenReturn(validProduct);

        when(dummyApiClient.validateProfileForAProduct(requestDto, Product.QB_PAYMENTS))
                .thenReturn(invalidProduct);

        ValidationResultDto validationResult = businessProfileValidationService.validateBusinessProfileForSubscribedProducts(
                requestDto, subscribedProducts);

        // Assert that the validation result is invalid
        assertFalse(validationResult.getIsValid());
        assertNotNull(validationResult.getRejectionReason());

        // Verify interactions with dummyApiClient
        verify(dummyApiClient, times(subscribedProducts.size()))
                .validateProfileForAProduct(eq(requestDto), any(Product.class));
    }

    @Test
    public void testValidateProfileForAProductAsync() throws Exception {
        // Prepare test data
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        Product product = Product.QB;
        ProductValidationResultDto expectedResult = new ProductValidationResultDto(true, "Valid product", Product.QB);

        // Mock dummyApiClient response
        when(dummyApiClient.validateProfileForAProduct(eq(requestDto), eq(product))).thenReturn(expectedResult);

        // Call the method under test
        CompletableFuture<ProductValidationResultDto> resultFuture =
                businessProfileValidationService.validateProfileForAProductAsync(requestDto, product);

        // Assert the result future
        assertNotNull(resultFuture);
        ProductValidationResultDto actualResult = resultFuture.get();
        assertNotNull(actualResult);
        assertEquals(expectedResult.getIsValid(), actualResult.getIsValid());
        assertEquals(expectedResult.getRejectionReason(), actualResult.getRejectionReason());

        // Verify interactions with dummyApiClient
        verify(dummyApiClient).validateProfileForAProduct(eq(requestDto), eq(product));
    }

    @Test
    public void testGetUserAndListOfSubscribedProducts_UserNotFound() {
        String userId = "user123";

        when(userService.getUser(eq(userId))).thenReturn(null);

        // Call the method under test and expect an exception
        assertThrows(EntityNotFoundException.class,
                () -> businessProfileValidationService.getUserAndListOfSubscribedProducts(userId));

    }

    @Test
    public void testGetUserAndListOfSubscribedProducts_UserNotSubscribed() {
        String userId = "user123";
        User user = new User();
        user.setSubscribedProducts(new HashSet<>());

        when(userService.getUser(eq(userId))).thenReturn(user);

        assertThrows(IllegalArgumentException.class,
                () -> businessProfileValidationService.getUserAndListOfSubscribedProducts(userId));

    }

    @Test
    public void testGetUserAndListOfSubscribedProducts_Success() {
        // Prepare test data
        String userId = "user123";
        Set<Product> subscribedProducts = Set.of(Product.QB);
        User user = new User();
        user.setSubscribedProducts(subscribedProducts);

        when(userService.getUser(eq(userId))).thenReturn(user);

        Pair<User, Set<Product>> result = businessProfileValidationService.getUserAndListOfSubscribedProducts(userId);

        // Assert the result
        assertNotNull(result);
        assertEquals(user, result.getFirst());
        assertEquals(subscribedProducts, result.getSecond());

        // Verify interaction with userService
        verify(userService).getUser(eq(userId));
    }

    @Test
    public void testCreateBusinessProfileValidationTaskAsync() {
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        requestDto.setValidationId("validation123");
        requestDto.setValidationRequestEventTime(Instant.now().toEpochMilli());
        requestDto.setProfileId("profile123");
        requestDto.setCompanyName("Company ABC");
        requestDto.setLegalName("Legal Name");

        doNothing().when(asyncCacheUpdationService).updateLatestValidationByProfileIdCache(eq("profile123"));

        businessProfileValidationService.createBusinessProfileValidationTaskAsync(requestDto);

        // Verify interactions with repository and asyncCacheUpdationService
        verify(businessProfileValidationRepository).createBusinessProfileValidationTask(any());
        verify(asyncCacheUpdationService).updateLatestValidationByProfileIdCache(eq("profile123"));
    }

    @Test
    public void testCreateBusinessProfileValidationTaskSync() {
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        requestDto.setValidationId("validation123");
        requestDto.setValidationRequestEventTime(Instant.now().toEpochMilli());
        requestDto.setProfileId("profile123");
        requestDto.setCompanyName("Company ABC");
        requestDto.setLegalName("Legal Name");

        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();
        businessProfileValidation.setProfileId("profile123");
        businessProfileValidation.setValidationId("validation123");
        businessProfileValidation.setStatus(Status.IN_PROGRESS);

        doNothing().when(asyncCacheUpdationService).updateLatestValidationByProfileIdCache(eq("profile123"));
        when(businessProfileValidationRepository.createBusinessProfileValidationTask(any())).thenReturn(businessProfileValidation);


        BusinessProfileValidation result = businessProfileValidationService.createBusinessProfileValidationTaskSync(requestDto);

        assertNotNull(result);
        assertEquals(Status.IN_PROGRESS, result.getStatus());
        assertEquals(requestDto.getValidationId(), result.getValidationId());

        // Verify interactions with repository and asyncCacheUpdationService
        verify(businessProfileValidationRepository).createBusinessProfileValidationTask(any());
        verify(asyncCacheUpdationService).updateLatestValidationByProfileIdCache(eq("profile123"));
    }

    @Test
    public void testCreateBusinessProfileValidationObject() {
        BusinessProfileCreateUpdateValidationRequestDto requestDto = new BusinessProfileCreateUpdateValidationRequestDto();
        requestDto.setValidationId("validation123");
        requestDto.setValidationRequestEventTime(Instant.now().toEpochMilli());
        requestDto.setProfileId("profile123");
        requestDto.setCompanyName("Company ABC");
        requestDto.setLegalName("Legal Name");
        requestDto.setBusinessAddress(new Address());
        requestDto.setLegalAddress(new Address());
        requestDto.setTaxIdentifiers(new TaxIdentifiers());
        requestDto.setEmail("test@example.com");
        requestDto.setWebsite("www.example.com");

        BusinessProfileValidation result = businessProfileValidationService.createBusinessProfileValidationObject(requestDto);

        assertNotNull(result);
        assertEquals(requestDto.getValidationId(), result.getValidationId());
        assertEquals(requestDto.getValidationRequestEventTime(), result.getValidationRequestEventTime());
        assertEquals(Status.IN_PROGRESS, result.getStatus());
        assertNull(result.getRejectionReason());
        assertNull(result.getFailureReason());
        assertNull(result.getTerminationReason());
        assertEquals(requestDto.getProfileId(), result.getProfileId());
        assertEquals(requestDto.getCompanyName(), result.getCompanyName());
        assertEquals(requestDto.getLegalName(), result.getLegalName());
        assertEquals(requestDto.getBusinessAddress(), result.getBusinessAddress());
        assertEquals(requestDto.getLegalAddress(), result.getLegalAddress());
        assertEquals(requestDto.getTaxIdentifiers(), result.getTaxIdentifiers());
        assertEquals(requestDto.getEmail(), result.getEmail());
        assertEquals(requestDto.getWebsite(), result.getWebsite());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

}