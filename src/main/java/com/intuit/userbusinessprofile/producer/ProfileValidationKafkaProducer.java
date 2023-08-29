package com.intuit.userbusinessprofile.producer;

import com.intuit.userbusinessprofile.constant.ApplicationConstant;
import com.intuit.userbusinessprofile.dto.BusinessProfileCreateUpdateValidationRequestDto;
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
    private KafkaTemplate<String, BusinessProfileCreateUpdateValidationRequestDto> kafkaTemplate;

    @Autowired
    ProfileValidationKafkaProducer(KafkaTemplate<String, BusinessProfileCreateUpdateValidationRequestDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(BusinessProfileCreateUpdateValidationRequestDto message) {

        CompletableFuture<SendResult<String, BusinessProfileCreateUpdateValidationRequestDto>> future =
                kafkaTemplate.send(ApplicationConstant.TOPIC_NAME, message);
        future.whenComplete((result, ex) -> {

            if (ex == null) {
                logger.info("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                logger.info("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            }
        });
    }

}
