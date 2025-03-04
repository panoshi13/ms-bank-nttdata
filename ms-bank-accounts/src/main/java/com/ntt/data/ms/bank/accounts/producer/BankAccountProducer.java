package com.ntt.data.ms.bank.accounts.producer;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.data.ms.bank.accounts.dto.UpdateYankiDTO;
import com.ntt.data.ms.bank.accounts.entity.DebitCard;
import com.ntt.data.ms.bank.accounts.model.DebitCardAssociateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class BankAccountProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankAccountProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public BankAccountProducer(@Qualifier("kafkaTemplate")
                               KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(DebitCardAssociateRequest debitCardAssociateRequest) {
        LOGGER.info("Producing message for debit card {}",
            debitCardAssociateRequest.getDebitCardId());

        // Crear el JSON manualmente
        String message = createJsonMessage(debitCardAssociateRequest.getDebitCardId(),
            debitCardAssociateRequest.getDocumentNumber(),
            debitCardAssociateRequest.getBalance());

        // Enviar el mensaje como String (JSON) al tópico
        this.kafkaTemplate.send("ASSOCIATE-WALLET-DEBIT", message);
    }


    public void sendMessageUpdateYanki(UpdateYankiDTO updateYankiDTO) {
        LOGGER.info("Producing message for update yanki {}", updateYankiDTO);

        try {
            // Usamos ObjectMapper para construir el JSON a partir de un mapa
            var message = objectMapper.writeValueAsString(updateYankiDTO);
            // Enviar el mensaje como String (JSON) al tópico
            this.kafkaTemplate.send("UPDATE-WALLET-DEBIT", message);
        } catch (Exception e) {
            LOGGER.error("Error updating JSON message: {}", e.getMessage());
        }


    }

    private String createJsonMessage(String debitCardId, String documentNumber,
                                     BigDecimal balance) {
        try {
            var debitCard = new DebitCardAssociateRequest();
            debitCard.setDebitCardId(debitCardId);
            debitCard.setDocumentNumber(documentNumber);
            debitCard.setBalance(balance);
            // Usamos ObjectMapper para construir el JSON a partir de un mapa
            return objectMapper.writeValueAsString(debitCard);
        } catch (Exception e) {
            LOGGER.error("Error creating JSON message: {}", e.getMessage());
            return "{}";  // Retorna un JSON vacío en caso de error
        }
    }
}
