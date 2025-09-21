package ru.yandex.practicum.payment_service.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.payment_service.config.EmbeddedRedisConfiguration;
import ru.yandex.practicum.payment_service.model.exception.BadRequestException;
import ru.yandex.practicum.payment_service.model.exception.IllegalArgumentException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedRedisConfiguration.class)
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Value("${payment.balance.amount.default:10000}")
    private Integer defaultBalance;
    @Test
    void getBalance_WhenBalanceExists_ShouldReturnBalance() {

        // Act & Assert
        paymentService.getBalance()
                .doOnSuccess(balance -> {
            assertNotNull(balance);
            assertEquals(balance, defaultBalance);
        }).block();
    }

    @Test
    void setBalance_WithNullAmount_ShouldReturnError() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.setBalance(null).block();
        });
    }

    @Test
    void setBalance_WithNegativeAmount_ShouldReturnError() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.setBalance(-100).block();
        });
    }


    @Test
    void setBalance_WithExistingBalance_ShouldAddToCurrentBalance() {
        Integer currentBalance = 5000;
        Integer amount = 2000;
        //устанавливаем баланс
        paymentService.setValueToBalance(currentBalance).block();

        paymentService.setBalance(amount).doOnSuccess(newBalance -> {
            assertNotNull(newBalance);
            assertEquals(newBalance, currentBalance + amount);
        }).block();
    }

    @Test
    void setBalance_WhenExceedingLimit_ShouldReturnError() {
        // Arrange
        Integer amount = 2000;
        //устанавливаем баланс
        paymentService.setValueToBalance(defaultBalance).block();

        assertThrows(BadRequestException.class, () -> {
            paymentService.setBalance(amount).block();
        });
    }

    @Test
    void processPayment_WithNullAmount_ShouldReturnError() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(null).block();
        });
    }

    @Test
    void processPayment_WithNegativeAmount_ShouldReturnError() {
        assertThrows(IllegalArgumentException.class, () -> {
            paymentService.processPayment(-100).block();
        });
    }

    @Test
    void processPayment_WithSufficientFunds_ShouldProcessSuccessfully() {
        Integer currentBalance = 5000;
        Integer debitAmount = 2000;

        //устанавливаем баланс
        paymentService.setValueToBalance(currentBalance).block();
        paymentService.processPayment(debitAmount).doOnSuccess(transaction -> {
            assertNotNull(transaction);
            assertEquals(true, transaction.getSuccess());
            assertEquals(currentBalance - debitAmount, transaction.getBalance());
            assertNull(transaction.getMessage());
        }).block();

    }

}
