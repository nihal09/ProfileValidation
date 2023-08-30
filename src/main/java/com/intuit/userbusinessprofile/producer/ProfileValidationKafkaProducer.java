package com.intuit.userbusinessprofile.producer;

import com.intuit.userbusinessprofile.constant.ApplicationConstant;
import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
import com.intuit.userbusinessprofile.model.RetryProfileValidationRequest;
import com.intuit.userbusinessprofile.service.RetryProfileValidationRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ProfileValidationKafkaProducer {
    private static final Logger logger = LoggerFactory.getLogger(ProfileValidationKafkaProducer.class);
    private final KafkaTemplate<String, BusinessProfileCreateUpdateValidationRequestDto> kafkaTemplate;

    private final RetryProfileValidationRequestService retryProfileValidationRequestService;

    @Autowired
    ProfileValidationKafkaProducer(KafkaTemplate<String, BusinessProfileCreateUpdateValidationRequestDto> kafkaTemplate, RetryProfileValidationRequestService retryProfileValidationRequestService) {
        this.kafkaTemplate = kafkaTemplate;
        this.retryProfileValidationRequestService = retryProfileValidationRequestService;
    }

    public void sendMessage(BusinessProfileCreateUpdateValidationRequestDto message) {

        CompletableFuture<SendResult<String, BusinessProfileCreateUpdateValidationRequestDto>> future =
                kafkaTemplate.send(ApplicationConstant.TOPIC_NAME, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                // adding this to a table to push it later to kafka using cron
                RetryProfileValidationRequest request = new RetryProfileValidationRequest();
                request.setRetryRequest(message);
                retryProfileValidationRequestService.createRetryProfileValidationRequest(request);
                logger.info("Unable to send message=[" + message + "] due to : " + ex.getMessage());

            }
        });
    }

}
