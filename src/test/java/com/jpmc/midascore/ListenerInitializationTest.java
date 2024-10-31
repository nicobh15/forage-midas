package com.jpmc.midascore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jpmc.midascore.component.TransactionListener;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ListenerInitializationTest {

    @Autowired
    private TransactionListener transactionListener;

    @Test
    void contextLoads() {
        assertNotNull(transactionListener, "TransactionListener should be initialized");
    }
}
