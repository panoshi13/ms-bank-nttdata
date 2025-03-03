package com.ntt.data.ms.user.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntt.data.ms.user.consumer.dto.DebitCardAssociateDTO;
import com.ntt.data.ms.user.entity.DebitCard;
import com.ntt.data.ms.user.service.UserService;
import lombok.RequiredArgsConstructor;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserWalletConsumer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final UserService userService;

    Logger logger = LoggerFactory.getLogger(UserWalletConsumer.class);

    @KafkaListener(topics = "ASSOCIATE-WALLET-DEBIT", groupId = "group_id")
    public void consume(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            DebitCardAssociateDTO dto = objectMapper.readValue(message, DebitCardAssociateDTO.class);
            logger.info("Consuming Message {}", dto);

            DebitCard debitCard = new DebitCard();
            debitCard.setDebitCardId(dto.getDebitCardId());
            debitCard.setBalance(dto.getBalance());

            userService.findByDocumentNumber(dto.getDocumentNumber())
                .flatMap(user -> {
                    if (user.getDebitCard() != null) {
                        logger.info("User already has a debit card");
                        kafkaTemplate.send("response-topic", dto.getDebitCardId() + ":REJECTED");
                        return Maybe.empty();
                    }
                    user.setDebitCard(debitCard);
                    user.setBalance(dto.getBalance());
                    // Enviar confirmación a Kafka
                    kafkaTemplate.send("response-topic", dto.getDebitCardId() + ":CONFIRMED");
                    return userService.updateUser(user).toMaybe();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                    updatedUser -> logger.info("User updated: {}", updatedUser),
                    throwable -> logger.error("Error updating user", throwable),
                    () -> logger.info("User document number: {}", dto.getDocumentNumber())
                );

        } catch (IOException e) {
            logger.error("Error deserializing message", e);
        }
    }

}
