package com.intuit.userbusinessprofile.service.impl;

import com.intuit.userbusinessprofile.dto.ProductValidationResultDto;
import com.intuit.userbusinessprofile.dto.ValidationResultDto;
import com.intuit.userbusinessprofile.model.*;
import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.repository.BusinessProfileRepository;
import com.intuit.userbusinessprofile.repository.BusinessProfileValidationRepository;
import com.intuit.userbusinessprofile.repository.UserRepository;
import com.intuit.userbusinessprofile.service.BusinessProfileValidationService;
import com.intuit.userbusinessprofile.service.DummyApiClient;
import com.intuit.userbusinessprofile.service.TransactionService;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BusinessProfileValidationServiceImpl implements BusinessProfileValidationService {

    private final BusinessProfileValidationRepository businessProfileValidationRepository;
    private final UserRepository userRepository;
    private final DummyApiClient dummyApiClient;
    private final TransactionService transactionService;
    private final BusinessProfileRepository businessProfileRepository;

    public BusinessProfileValidationServiceImpl(BusinessProfileValidationRepository businessProfileValidationRepository, UserRepository userRepository, DummyApiClient dummyApiClient, TransactionService transactionService, BusinessProfileRepository businessProfileRepository) {
        this.businessProfileValidationRepository = businessProfileValidationRepository;
        this.userRepository = userRepository;
        this.dummyApiClient = dummyApiClient;
        this.transactionService = transactionService;
        this.businessProfileRepository = businessProfileRepository;
    }

    @Override
    public void validateAndUpdateBusinessProfileIfRequired(BusinessProfileCreateUpdateValidationRequestDto request) throws ExecutionException, InterruptedException {
        try {
            String businessProfileId = request.getProfileId();
            BusinessProfileValidation businessProfileValidation = createBusinessProfileValidationTask(request);
            if (businessProfileValidation.getStatus() == Status.IN_PROGRESS) {
                try {
                    Pair<User, Set<Product>> userAndSubscribedProducts = getUserAndListOfSubscribedProducts(request.getUserId());
                    Set<Product> subscribedProducts = userAndSubscribedProducts.getSecond();
                    User user = userAndSubscribedProducts.getFirst();
                    ValidationResultDto validationResult = validateBusinessProfileForSubscribedProducts(request, subscribedProducts);
                    if (validationResult.getIsValid()) {
                        switch (request.getBusinessProfileTaskType()) {
                            case CREATE -> {
                                if (businessProfileId != null)
                                    throw new IllegalArgumentException("Business Profile already Exist with id - " + businessProfileId);
                                createProfileUpdateUserAndValidationTaskInTransaction(request, businessProfileValidation, user);
                            }
                            case UPDATE -> {
                                if (businessProfileId == null)
                                    throw new IllegalArgumentException("Business Profile Id cannot be null to update business profile");
                                BusinessProfile businessProfile = businessProfileRepository.getBusinessProfile(businessProfileId);
                                if (businessProfile == null)
                                    throw new IllegalArgumentException("Business Profile doesn't exist for profileId - " + businessProfileId);

                                updateProfileAndTaskCreateProfileHistoryIfRequired(businessProfile, request, businessProfileValidation);
                            }
                        }
                    } else {
                        rejectBusinessProfileValidationTask(businessProfileValidation, validationResult.getRejectionReason());
                    }
                } catch (NotFoundException | IllegalArgumentException e) {
                    businessProfileValidation.setStatus(Status.FAILED);
                    businessProfileValidation.setRejectionReason(e.getMessage());
                    businessProfileValidationRepository.updateBusinessProfileValidationTask(businessProfileValidation);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public BusinessProfileValidation getBusinessProfileValidation(String validationId) {
        return businessProfileValidationRepository.getBusinessProfileValidation(validationId);
    }

    public void rejectBusinessProfileValidationTask(BusinessProfileValidation businessProfileValidation, String rejectionReason){
        businessProfileValidation.setStatus(Status.REJECTED);
        businessProfileValidation.setRejectionReason(rejectionReason);
        businessProfileValidationRepository.updateBusinessProfileValidationTask(businessProfileValidation);
    }
    public void updateProfileAndTaskCreateProfileHistoryIfRequired(
            BusinessProfile businessProfile,
            BusinessProfileCreateUpdateValidationRequestDto request,
            BusinessProfileValidation businessProfileValidation
    ) {
        if (businessProfile.getLatestApprovedValidationEventTime() < request.getValidationRequestEventTime()) {
            updateProfileAndTaskCreateProfileHistoryInTransaction(businessProfile, request, businessProfileValidation);
            updateBusinessProfileCache(businessProfile.getProfileId());
            updateBusinessProfileValidationCache(businessProfileValidation.getValidationId());
        } else {
            businessProfileValidation.setStatus(Status.TERMINATED);
            businessProfileValidation.setTerminationReason(TerminationReason.PROFILE_ALREADY_UPDATED_WITH_NEW_REQUEST);
            businessProfileValidationRepository.updateBusinessProfileValidationTask(businessProfileValidation);
        }
    }

    public void updateProfileAndTaskCreateProfileHistoryInTransaction(
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

        transactionService.updateProfileAndTaskCreateProfileHistoryInTransaction(businessProfileHistory, businessProfile, businessProfileValidation);

    }

    public void createProfileUpdateUserAndValidationTaskInTransaction(
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
        businessProfileValidation.setUpdatedAt(currentTimeInEpocMs);

        transactionService.createProfileUpdateUserAndValidationTaskInTransaction(user, businessProfile, businessProfileValidation);
        updateUserCache(user.getUserId());
        updateBusinessProfileCache(businessProfileId);
        updateBusinessProfileValidationCache(businessProfileValidation.getValidationId());
    }

    @Async
    public void updateUserCache(String userId) {
        userRepository.getUserAndUpdateCache(userId);
    }

    @Async
    public void updateBusinessProfileCache(String profileId) {
        businessProfileRepository.getBusinessProfileAndUpdateCache(profileId);
    }

    @Async
    public void updateBusinessProfileValidationCache(String validationId) {
        businessProfileValidationRepository.getBusinessProfileValidationAndUpdateCache(validationId);
    }

    @Override
    public User test(String key) {
        User test = new User();
        Set<Product> products= new HashSet<>();
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
        return userRepository.createUser(test);
    }

    public ValidationResultDto validateBusinessProfileForSubscribedProducts(BusinessProfileCreateUpdateValidationRequestDto request, Set<Product> subscribedProducts) throws ExecutionException, InterruptedException {
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
    public CompletableFuture<ProductValidationResultDto> validateProfileForAProductAsync(BusinessProfileCreateUpdateValidationRequestDto request, Product product) {
        ProductValidationResultDto validationResult = dummyApiClient.validateProfileForAProduct(request, product);
        return CompletableFuture.completedFuture(validationResult);
    }

    public Pair<User, Set<Product>> getUserAndListOfSubscribedProducts(String userId) {
        User user = userRepository.getUser(userId);
        if (user == null)
            throw new NotFoundException("User with Id -" + userId + " not found");
        Set<Product> subscribedProducts = user.getSubscribedProducts();
        if (subscribedProducts.isEmpty())
            throw new IllegalArgumentException("User is not subscribed to any product");
        else return Pair.of(user, user.getSubscribedProducts());
    }

    public BusinessProfileValidation createBusinessProfileValidationTask(BusinessProfileCreateUpdateValidationRequestDto request) {
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
        return businessProfileValidationRepository.createBusinessProfileValidationTask(businessProfileValidation);

    }

}
