package com.intuit.userbusinessprofile.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.userbusinessprofile.constant.ApplicationConstant;
import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.service.BusinessProfileValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProfileValidationKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProfileValidationKafkaConsumer.class);

    private final BusinessProfileValidationService businessProfileValidationService;

    @Autowired
    public ProfileValidationKafkaConsumer(BusinessProfileValidationService businessProfileValidationService) {
        this.businessProfileValidationService = businessProfileValidationService;
    }

    @KafkaListener(groupId = ApplicationConstant.GROUP_ID_JSON, topics = ApplicationConstant.TOPIC_NAME, containerFactory = ApplicationConstant.KAFKA_LISTENER_CONTAINER_FACTORY)
    public void receivedMessage(BusinessProfileCreateUpdateValidationRequestDto message) throws JsonProcessingException {
        try {
            businessProfileValidationService.validateAndUpdateBusinessProfileIfRequired(message);
        } catch (Exception e){
            logger.info("Json message received using Kafka listener error");
        }
        logger.info("Json message received using Kafka listener " + message);
    }
}