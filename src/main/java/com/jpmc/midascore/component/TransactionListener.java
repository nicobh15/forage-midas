package com.jpmc.midascore.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.incentive.Incentive;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TransactionListener {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    public TransactionListener() {
        System.out.println(
                "TransactionListener initialized and ready to listen for messages on '${general.kafka-topic}'");
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "transaction-group")
    public void listen(Transaction transaction) {
        logger.info("Received transaction: " + transaction.toString());
        logger.info("Processing transaction...");

        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());

        if (sender != null && recipient != null && sender.getBalance() >= transaction.getAmount()) {
            logger.info("Transaction validated. Processing...");
            sender.setBalance(sender.getBalance() - transaction.getAmount());

            Incentive incentive = fetchIncentive(transaction);

            recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentive.getAmount());

            TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount(),
                    incentive.getAmount());
            userRepository.save(sender);
            userRepository.save(recipient);
            transactionRepository.save(record);
            logger.info("Transaction processed successfully.");

            logger.info("Sender balance: " + sender.getName() + sender.getBalance());
            logger.info("Recipient balance: " + recipient.getName() + recipient.getBalance());
        } else {
            logger.info("Transaction validation failed. Discarding transaction.");
            System.out.println("Transaction discarded due to validation failure.");
        }
    }

    private Incentive fetchIncentive(Transaction transaction) {
        String url = "http://localhost:8080/incentive";
        return restTemplate.postForObject(url, transaction, Incentive.class);
    }
}
