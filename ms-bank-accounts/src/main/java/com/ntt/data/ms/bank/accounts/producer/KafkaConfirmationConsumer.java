package com.ntt.data.ms.bank.accounts.producer;

import com.ntt.data.ms.bank.accounts.response.KafkaResponseService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConfirmationConsumer {
    private final KafkaResponseService responseService;

    public KafkaConfirmationConsumer(KafkaResponseService responseService) {
        this.responseService = responseService;
    }

    @KafkaListener(topics = "response-topic", groupId = "group_id")
    public void consumeConfirmation(String message) {
        String[] parts = message.split(":");
        String messageId = parts[0];
        String status = parts[1];

        // Completar la solicitud en espera
        responseService.completeRequest(messageId, status);
    }

}
