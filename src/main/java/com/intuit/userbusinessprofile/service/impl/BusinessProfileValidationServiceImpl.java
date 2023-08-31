package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.dto.*;
import com.intuit.userbusinessprofile.dto.enums.BusinessProfileTaskType;
import com.intuit.userbusinessprofile.exceptions.ArgumentNotValidException;
import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import com.intuit.userbusinessprofile.model.*;
import com.intuit.userbusinessprofile.producer.ProfileValidationKafkaProducer;
import com.intuit.userbusinessprofile.repository.BusinessProfileValidationRepository;
import com.intuit.userbusinessprofile.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BusinessProfileValidationServiceImpl implements BusinessProfileValidationService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessProfileValidationServiceImpl.class);
    private final BusinessProfileValidationRepository businessProfileValidationRepository;
    private final UserService userService;
    private final DummyApiClient dummyApiClient;
    private final TransactionService transactionService;
    private final BusinessProfileService businessProfileService;
    private final ProfileValidationKafkaProducer profileValidationKafkaProducer;

    private final AsyncCacheUpdationService asyncCacheUpdationService;

    @Autowired
    public BusinessProfileValidationServiceImpl(BusinessProfileValidationRepository businessProfileValidationRepository, UserService userService, DummyApiClient dummyApiClient, TransactionService transactionService, BusinessProfileService businessProfileService, ProfileValidationKafkaProducer profileValidationKafkaProducer, AsyncCacheUpdationService asyncCacheUpdationService) {
        this.businessProfileValidationRepository = businessProfileValidationRepository;
        this.userService = userService;
        this.dummyApiClient = dummyApiClient;
        this.transactionService = transactionService;
        this.businessProfileService = businessProfileService;
        this.profileValidationKafkaProducer = profileValidationKafkaProducer;
        this.asyncCacheUpdationService = asyncCacheUpdationService;
    }

    @Override
    public void validateAndUpdateBusinessProfileIfRequired(BusinessProfileCreateUpdateValidationRequestDto request) throws ExecutionException, InterruptedException {
        try {
            String businessProfileId = request.getProfileId();
            // taking pessimistic approach, validation item may have not been created
            BusinessProfileValidation nullableBusinessProfileValidation = getBusinessProfileValidationWithoutNullCheck(request.getValidationId());
            BusinessProfileValidation businessProfileValidation = nullableBusinessProfileValidation == null ? createBusinessProfileValidationTaskSync(request) : nullableBusinessProfileValidation;
            logger.info("Processing Validation Task Id - "+businessProfileValidation.getValidationId());
            if (businessProfileValidation.getStatus() == Status.IN_PROGRESS) {
                try {
                    Pair<User, Set<Product>> userAndSubscribedProducts = getUserAndListOfSubscribedProducts(request.getUserId());
                    Set<Product> subscribedProducts = userAndSubscribedProducts.getSecond();
                    User user = userAndSubscribedProducts.getFirst();
                    ValidationResultDto validationResult = validateBusinessProfileForSubscribedProducts(request, subscribedProducts);
                    if (validationResult.getIsValid()) {
                        switch (request.getBusinessProfileTaskType()) {
                            case CREATE -> {
                                if (user.getBusinessProfileId() != null)
                                    throw new IllegalArgumentException("Business Profile already Exist with id - " + user.getBusinessProfileId());
                                createProfileUpdateUserAndValidationTaskInTransaction(request, businessProfileValidation, user);
                            }
                            case UPDATE -> {
                                BusinessProfile businessProfile = businessProfileService.getBusinessProfile(businessProfileId);
                                if (businessProfile == null)
                                    throw new IllegalArgumentException("Business Profile doesn't exist for profileId - " + businessProfileId);

                                updateProfileAndTaskCreateProfileHistoryIfRequired(businessProfile, request, businessProfileValidation);
                            }
                        }
                    } else {
                        rejectBusinessProfileValidationTask(businessProfileValidation, validationResult.getRejectionReason());
                    }
                } catch (EntityNotFoundException | IllegalArgumentException e) {
                    businessProfileValidation.setStatus(Status.FAILED);
                    businessProfileValidation.setRejectionReason(e.getMessage());
                    updateBusinessProfileValidationTask(businessProfileValidation);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public BusinessProfileValidation getBusinessProfileValidationWithoutNullCheck(String validationId) {
        return businessProfileValidationRepository.getBusinessProfileValidation(validationId);
    }


    @Override
    public BusinessProfileValidation updateBusinessProfileValidationTask(BusinessProfileValidation businessProfileValidation) {
        businessProfileValidation.setUpdatedAt(Instant.now().toEpochMilli());
        businessProfileValidationRepository.updateBusinessProfileValidationTask(businessProfileValidation);
        return businessProfileValidation;
    }

    @Override
    public void initiateValidationForBusinessProfileCreation(BusinessProfileValidateAndCreateRequestDto requestDto, String validationId) {
        BusinessProfileCreateUpdateValidationRequestDto request = new BusinessProfileCreateUpdateValidationRequestDto();
        request.setUserId(requestDto.getUserId());
        request.setValidationId(validationId);
        request.setProfileId(null);
        request.setCompanyName(requestDto.getCompanyName());
        request.setLegalName(requestDto.getLegalName());
        request.setBusinessAddress(requestDto.getBusinessAddress());
        request.setLegalAddress(requestDto.getLegalAddress());
        request.setTaxIdentifiers(requestDto.getTaxIdentifiers());
        request.setEmail(requestDto.getEmail());
        request.setWebsite(requestDto.getWebsite());
        request.setValidationRequestEventTime(requestDto.getRequestTime());
        request.setBusinessProfileTaskType(BusinessProfileTaskType.CREATE);

        createBusinessProfileValidationTaskAsync(request);
        sendValidationRequestToKafka(request);
    }

    @Override
    public void initiateValidationForBusinessProfileUpdation(BusinessProfileValidateAndUpdateRequestDto requestDto, String validationId) {
        if (requestDto.getProfileId() == null)
            throw new ArgumentNotValidException("ProfileId cannot be null");
        BusinessProfileCreateUpdateValidationRequestDto request = new BusinessProfileCreateUpdateValidationRequestDto();
        request.setUserId(requestDto.getUserId());
        request.setValidationId(validationId);
        request.setProfileId(requestDto.getProfileId());
        request.setCompanyName(requestDto.getCompanyName());
        request.setLegalName(requestDto.getLegalName());
        request.setBusinessAddress(requestDto.getBusinessAddress());
        request.setLegalAddress(requestDto.getLegalAddress());
        request.setTaxIdentifiers(requestDto.getTaxIdentifiers());
        request.setEmail(requestDto.getEmail());
        request.setWebsite(requestDto.getWebsite());
        request.setValidationRequestEventTime(requestDto.getRequestTime());
        request.setBusinessProfileTaskType(BusinessProfileTaskType.UPDATE);

        // Simulating API call
        createBusinessProfileValidationTaskAsync(request);
        sendValidationRequestToKafka(request);
    }

    @Async
    protected void sendValidationRequestToKafka(BusinessProfileCreateUpdateValidationRequestDto request) {
        profileValidationKafkaProducer.sendMessage(request);
    }

    void rejectBusinessProfileValidationTask(BusinessProfileValidation businessProfileValidation, String rejectionReason) {
        businessProfileValidation.setStatus(Status.REJECTED);
        businessProfileValidation.setRejectionReason(rejectionReason);
        updateBusinessProfileValidationTask(businessProfileValidation);
        String profileId = businessProfileValidation.getProfileId();
        if (profileId != null)
            asyncCacheUpdationService.updateLatestValidationByProfileIdCache(profileId);
    }

    void updateProfileAndTaskCreateProfileHistoryIfRequired(
            BusinessProfile businessProfile,
            BusinessProfileCreateUpdateValidationRequestDto request,
            BusinessProfileValidation businessProfileValidation
    ) {
        if (businessProfile.getLatestApprovedValidationEventTime() < request.getValidationRequestEventTime()) {
            updateProfileAndTaskCreateProfileHistoryInTransaction(businessProfile, request, businessProfileValidation);
            asyncCacheUpdationService.updateBusinessProfileCache(businessProfile.getProfileId());
        } else {
            businessProfileValidation.setStatus(Status.TERMINATED);
            businessProfileValidation.setTerminationReason(TerminationReason.PROFILE_ALREADY_UPDATED_WITH_NEW_REQUEST);
            updateBusinessProfileValidationTask(businessProfileValidation);
        }
        asyncCacheUpdationService.updateLatestValidationByProfileIdCache(businessProfile.getProfileId());
    }

    void updateProfileAndTaskCreateProfileHistoryInTransaction(
            BusinessProfile businessProfile,
            BusinessProfileCreateUpdateValidationRequestDto request,
            BusinessProfileValidation businessProfileValidation
    ) {
        Long currentTime = Instant.now().toEpochMilli();

        BusinessProfileHistory businessProfileHistory = new BusinessProfileHistory();
        businessProfileHistory.setProfileId(businessProfile.getProfileId());
        businessProfileHistory.setCompanyName(businessProfile.getCompanyName());
        businessProfileHistory.setLegalName(businessProfile.getLegalName());
        businessProfileHistory.setBusinessAddress(businessProfile.getBusinessAddress());
        businessProfileHistory.setLegalAddress(businessProfile.getLegalAddress());
        businessProfileHistory.setTaxIdentifiers(businessProfile.getTaxIdentifiers());
        businessProfileHistory.setEmail(businessProfile.getEmail());
        businessProfileHistory.setWebsite(businessProfile.getWebsite());
        businessProfileHistory.setStartedOn(businessProfile.getUpdatedAt());
        businessProfileHistory.setExpiredOn(currentTime);

        businessProfile.setCompanyName(request.getCompanyName());
        businessProfile.setLegalName(request.getLegalName());
        businessProfile.setBusinessAddress(request.getBusinessAddress());
        businessProfile.setLegalAddress(request.getLegalAddress());
        businessProfile.setTaxIdentifiers(request.getTaxIdentifiers());
        businessProfile.setEmail(request.getEmail());
        businessProfile.setWebsite(request.getWebsite());
        businessProfile.setUpdatedAt(currentTime);
        businessProfile.setLatestApprovedValidationEventTime(request.getValidationRequestEventTime());

        businessProfileValidation.setUpdatedAt(currentTime);
        businessProfileValidation.setStatus(Status.ACCEPTED);

        logger.info("Updating Business Profile with id - "+businessProfile.getProfileId());
        logger.info("Updating Validation task with id - "+businessProfileValidation.getValidationId());
        transactionService.updateProfileAndTaskCreateProfileHistoryInTransaction(businessProfileHistory, businessProfile, businessProfileValidation);
        logger.info("Updated Business Profile with id - "+businessProfile.getProfileId());
        logger.info("Updated Validation task with id- "+businessProfileValidation.getValidationId());
        logger.info("Created Profile History with id- "+businessProfileHistory.getId());

    }

    void createProfileUpdateUserAndValidationTaskInTransaction(
            BusinessProfileCreateUpdateValidationRequestDto request,
            BusinessProfileValidation businessProfileValidation,
            User user
    ) {

        Long currentTimeInEpocMs = Instant.now().toEpochMilli();

        BusinessProfile businessProfile = new BusinessProfile();
        String businessProfileId = UUID.randomUUID().toString();
        businessProfile.setProfileId(businessProfileId);
        businessProfile.setCompanyName(request.getCompanyName());
        businessProfile.setLegalName(request.getLegalName());
        businessProfile.setBusinessAddress(request.getBusinessAddress());
        businessProfile.setLegalAddress(request.getLegalAddress());
        businessProfile.setTaxIdentifiers(request.getTaxIdentifiers());
        businessProfile.setEmail(request.getEmail());
        businessProfile.setWebsite(request.getWebsite());
        businessProfile.setCreatedAt(currentTimeInEpocMs);
        businessProfile.setUpdatedAt(currentTimeInEpocMs);
        businessProfile.setLatestApprovedValidationEventTime(request.getValidationRequestEventTime());

        user.setBusinessProfileId(businessProfileId);
        user.setUpdatedAt(currentTimeInEpocMs);

        businessProfileValidation.setStatus(Status.ACCEPTED);
        businessProfileValidation.setProfileId(businessProfileId);
        businessProfileValidation.setUpdatedAt(currentTimeInEpocMs);

        logger.info("Creating Business Profile with id - "+businessProfileId);
        logger.info("Updating User id = "+ user.getUserId()+" with Business Profile id - "+businessProfileId);
        logger.info("Updating Validation task with id - "+businessProfileValidation.getValidationId());

        transactionService.createProfileUpdateUserAndValidationTaskInTransaction(user, businessProfile, businessProfileValidation);
        asyncCacheUpdationService.updateUserCache(user.getUserId());
        asyncCacheUpdationService.updateBusinessProfileCache(businessProfileId);
        asyncCacheUpdationService.updateBusinessProfileValidationCache(businessProfileValidation.getValidationId());

        logger.info("Created Business Profile with id - "+businessProfileId);
        logger.info("Updated User id = "+ user.getUserId()+" with Business Profile id - "+businessProfileId);
        logger.info("Updated Validation task with id - "+businessProfileValidation.getValidationId());
    }

    @Override
    public User test(String key) {
        User test = new User();
        Set<Product> products = new HashSet<>();
        products.add(Product.QB);
        test.setUserId(UUID.randomUUID().toString());
        test.setFirstName("jh");
        test.setLastName("h");
        test.setBusinessProfileId("gv");
        test.setSubscribedProducts(products);
        test.setCreatedAt(Instant.now().toEpochMilli());
        test.setUpdatedAt(Instant.now().toEpochMilli());

        /*User user = userRepository.getUser("96aa0a5c-5257-417c-b2f8-d8d60c92d56d");
        user.setBusinessProfileId("test");
        userRepository.test(user);*/
        return userService.createUser(test);
    }

    ValidationResultDto validateBusinessProfileForSubscribedProducts(BusinessProfileCreateUpdateValidationRequestDto request, Set<Product> subscribedProducts) throws ExecutionException, InterruptedException {
        List<CompletableFuture<ProductValidationResultDto>> futures = subscribedProducts.stream()
                .map(product -> validateProfileForAProductAsync(request, product))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<ProductValidationResultDto>> combinedFuture = allOf.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        );

        List<ProductValidationResultDto> validationResults = combinedFuture.get();
        List<ProductValidationResultDto> rejectedValidations =
                validationResults.stream().filter(validationResult -> !validationResult.getIsValid()).toList();
        boolean isValid = rejectedValidations.isEmpty();
        String rejectionReason = isValid ? null
                : rejectedValidations.stream().map(ProductValidationResultDto::toString).collect(Collectors.joining("\n "));
        return new ValidationResultDto(
                rejectedValidations.isEmpty(), rejectionReason
        );

    }

    @Async
    protected CompletableFuture<ProductValidationResultDto> validateProfileForAProductAsync(BusinessProfileCreateUpdateValidationRequestDto request, Product product) {
        ProductValidationResultDto validationResult = dummyApiClient.validateProfileForAProduct(request, product);
        return CompletableFuture.completedFuture(validationResult);
    }

    Pair<User, Set<Product>> getUserAndListOfSubscribedProducts(String userId) {
        User user = userService.getUser(userId);
        if (user == null)
            throw new EntityNotFoundException("User with Id -" + userId + " not found");
        Set<Product> subscribedProducts = user.getSubscribedProducts();
        if (subscribedProducts.isEmpty())
            throw new IllegalArgumentException("User is not subscribed to any product");
        else return Pair.of(user, user.getSubscribedProducts());
    }

    @Async
    protected void createBusinessProfileValidationTaskAsync(BusinessProfileCreateUpdateValidationRequestDto request) {
        BusinessProfileValidation businessProfileValidation = createBusinessProfileValidationObject(request);
        businessProfileValidationRepository.createBusinessProfileValidationTask(businessProfileValidation);
        if (request.getProfileId() != null)
            asyncCacheUpdationService.updateLatestValidationByProfileIdCache(request.getProfileId());
    }

    public BusinessProfileValidation createBusinessProfileValidationTaskSync(BusinessProfileCreateUpdateValidationRequestDto request) {
        BusinessProfileValidation businessProfileValidation = createBusinessProfileValidationObject(request);
        BusinessProfileValidation createdBusinessProfileValidation = businessProfileValidationRepository.createBusinessProfileValidationTask(businessProfileValidation);
        if (request.getProfileId() != null)
            asyncCacheUpdationService.updateLatestValidationByProfileIdCache(request.getProfileId());
        return createdBusinessProfileValidation;
    }

    BusinessProfileValidation createBusinessProfileValidationObject(BusinessProfileCreateUpdateValidationRequestDto request) {
        BusinessProfileValidation businessProfileValidation = new BusinessProfileValidation();
        Long currentTimeInEpochMs = Instant.now().toEpochMilli();
        businessProfileValidation.setValidationId(request.getValidationId());
        businessProfileValidation.setValidationRequestEventTime(request.getValidationRequestEventTime());
        businessProfileValidation.setStatus(Status.IN_PROGRESS);
        businessProfileValidation.setRejectionReason(null);
        businessProfileValidation.setFailureReason(null);
        businessProfileValidation.setTerminationReason(null);
        businessProfileValidation.setProfileId(request.getProfileId());
        businessProfileValidation.setCompanyName(request.getCompanyName());
        businessProfileValidation.setLegalName(request.getLegalName());
        businessProfileValidation.setBusinessAddress(request.getBusinessAddress());
        businessProfileValidation.setLegalAddress(request.getLegalAddress());
        businessProfileValidation.setTaxIdentifiers(request.getTaxIdentifiers());
        businessProfileValidation.setEmail(request.getEmail());
        businessProfileValidation.setWebsite(request.getWebsite());
        businessProfileValidation.setCreatedAt(currentTimeInEpochMs);
        businessProfileValidation.setUpdatedAt(currentTimeInEpochMs);
        return businessProfileValidation;
    }

}
