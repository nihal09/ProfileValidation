package com.intuit.userbusinessprofile.service;

import com.intuit.userbusinessprofile.dto.BusinessProfileValidationResultDto;
import com.intuit.userbusinessprofile.exceptions.EntityNotFoundException;
import com.intuit.userbusinessprofile.model.BusinessProfileValidation;
import com.intuit.userbusinessprofile.repository.BusinessProfileValidationRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@Testcontainers
@EnableCaching
public class BusinessProfileValidationStatusServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private BusinessProfileValidationRepository businessProfileValidationRepository;

    @InjectMocks
    private BusinessProfileValidationStatusService validationStatusService;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        redis.start();
    }


    @AfterAll
    public static void destroy() {
        redis.close();
    }

    @Container
    public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379);


    @Test
    public void testGetBusinessProfileValidationsByProfileId_NoValidationsFound() {
        String profileId = "123";
        when(businessProfileValidationRepository.getBusinessProfileValidationsByProfileId(profileId, 5))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class,
                () -> validationStatusService.getBusinessProfileValidationsByProfileId(profileId, 5));
    }

    @Test
    public void testGetBusinessProfileValidationsByProfileId_ValidationsFound() {
        String profileId = "123";
        when(businessProfileValidationRepository.getBusinessProfileValidationsByProfileId(profileId, 5))
                .thenReturn(Collections.singletonList(new BusinessProfileValidation()));
        when(modelMapper.map(any(BusinessProfileValidation.class), eq(BusinessProfileValidationResultDto.class)))
                .thenReturn(new BusinessProfileValidationResultDto());

        List<BusinessProfileValidationResultDto> validationResults =
                validationStatusService.getBusinessProfileValidationsByProfileId(profileId, 5);

        assertNotNull(validationResults);
        assertEquals(1, validationResults.size());
    }

    @Test
    public void testGetLatestBusinessProfileValidationByProfileIdFromDb_NoValidationsFound() {
        String profileId = "123";
        when(businessProfileValidationRepository.getBusinessProfileValidationsByProfileId(profileId, 1))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class,
                () -> validationStatusService.getLatestBusinessProfileValidationByProfileIdFromDb(profileId));
    }

    @Test
    public void testGetLatestBusinessProfileValidationByProfileIdFromDb_ValidationsFound() {
        String profileId = "123";
        when(businessProfileValidationRepository.getBusinessProfileValidationsByProfileId(profileId, 1))
                .thenReturn(Collections.singletonList(new BusinessProfileValidation()));
        when(modelMapper.map(any(BusinessProfileValidation.class), eq(BusinessProfileValidationResultDto.class)))
                .thenReturn(new BusinessProfileValidationResultDto());

        BusinessProfileValidationResultDto validationResult =
                validationStatusService.getLatestBusinessProfileValidationByProfileIdFromDb(profileId);

        assertNotNull(validationResult);
    }

    @Test
    public void testGetBusinessProfileValidationStatus() {
        String validationId = "xyz";
        BusinessProfileValidation validation = new BusinessProfileValidation();
        when(businessProfileValidationRepository.getBusinessProfileValidation(validationId))
                .thenReturn(validation);
        when(modelMapper.map(validation, BusinessProfileValidationResultDto.class))
                .thenReturn(new BusinessProfileValidationResultDto());

        BusinessProfileValidationResultDto validationResultDto =
                validationStatusService.getBusinessProfileValidationStatus(validationId);
        assertNotNull(validationResultDto);
    }

    @Test
    public void testGetBusinessProfileValidationStatus_ValidationNotFound() {
        String validationId = "xyz";
        when(businessProfileValidationRepository.getBusinessProfileValidation(validationId))
                .thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> validationStatusService.getBusinessProfileValidationStatus(validationId));
    }

    @Test
    public void testGetLatestBusinessProfileValidationByProfileId_UpdateCache() {
        String profileId = "123";
        List<BusinessProfileValidation> validations = Collections.singletonList(new BusinessProfileValidation());
        when(businessProfileValidationRepository.getBusinessProfileValidationsByProfileId(profileId, 1))
                .thenReturn(validations);
        when(modelMapper.map(any(BusinessProfileValidation.class), eq(BusinessProfileValidationResultDto.class)))
                .thenReturn(new BusinessProfileValidationResultDto());

        BusinessProfileValidationResultDto validationResult =
                validationStatusService.getLatestBusinessProfileValidationByProfileIdUpdateCache(profileId);

        assertNotNull(validationResult);

    }

    @Test
    public void testGetBusinessProfileValidationStatus_Cacheable() {
        String validationId = "xyz";
        BusinessProfileValidation validation = new BusinessProfileValidation();
        when(businessProfileValidationRepository.getBusinessProfileValidation(validationId))
                .thenReturn(validation);
        when(modelMapper.map(validation, BusinessProfileValidationResultDto.class))
                .thenReturn(new BusinessProfileValidationResultDto());

        BusinessProfileValidationResultDto firstCallResult =
                validationStatusService.getBusinessProfileValidationStatus(validationId);

        assertNotNull(firstCallResult);
    }
    @Test
    public void testGetBusinessProfileValidationAndUpdateCache() {
        String validationId = "xyz";
        BusinessProfileValidation validation = new BusinessProfileValidation();
        when(businessProfileValidationRepository.getBusinessProfileValidation(validationId))
                .thenReturn(validation);
        when(modelMapper.map(validation, BusinessProfileValidationResultDto.class))
                .thenReturn(new BusinessProfileValidationResultDto());

        BusinessProfileValidation updatedValidation =
                validationStatusService.getBusinessProfileValidationAndUpdateCache(validationId);

        assertNotNull(updatedValidation);
    }


}
