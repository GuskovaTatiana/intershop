package ru.yandex.practicum.payment_service.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment_service.model.PaymentTransaction;
import ru.yandex.practicum.payment_service.model.exception.BadRequestException;
import ru.yandex.practicum.payment_service.model.exception.IllegalArgumentException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ReactiveRedisTemplate<String, Integer> redisTemplate;

    @Value("${payment.balance.amount.default:10000}")
    private Integer defaultBalance;
    private static final String BALANCE_KEY = "balance";

    private Mono<Boolean> saveData(String key, Integer value) {
        return redisTemplate.opsForValue().set(key, value);
    }

    public Mono<Boolean> setValueToBalance(Integer value) {
        return redisTemplate.opsForValue().set(BALANCE_KEY, value);
    }

    private Mono<Integer> getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }


    // получение баланса
    public Mono<Integer> getBalance() {
        return getData(BALANCE_KEY)
                .switchIfEmpty(Mono.defer(() -> {
                    return saveData(BALANCE_KEY, defaultBalance)
                            .then(Mono.just(defaultBalance));
                }));
    }

    // пополнение баланса
    public Mono<Integer> setBalance(Integer amount) {
        // Проверка на null входящего параметра
        if (amount == null) {
            return Mono.error(new IllegalArgumentException("BAD_REQUEST", "Amount cannot be null"));
        }

        // Проверка на отрицательное значение
        if (amount < 0) {
            return Mono.error(new IllegalArgumentException("BAD_REQUEST", "Amount cannot be negative"));
        }

        return getData(BALANCE_KEY)
                .flatMap(currentBalance -> {
                    int current = currentBalance != null ? currentBalance : 0;
                    int newBalance = current + amount;

                    if (newBalance <= defaultBalance) {
                        return saveData(BALANCE_KEY, newBalance)
                                .then(Mono.just(newBalance));
                    } else {
                        return Mono.error(new BadRequestException("BALANCE_LIMIT_EXCEEDED", "Exceeding the deposit limit"));
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Если баланса нет в Redis, устанавливаем начальное значение
                    int initialBalance = Math.min(amount, defaultBalance);
                    return saveData(BALANCE_KEY, initialBalance)
                            .then(Mono.just(initialBalance));
                }));
    }

    // списание суммы с баланса
    public Mono<PaymentTransaction> processPayment(Integer debitAmount) {
        // Проверка на null входящего параметра
        if (debitAmount == null) {
            return Mono.error(new IllegalArgumentException("BAD_REQUEST", "Debit amount cannot be null"));
        }

        // Проверка на отрицательное значение
        if (debitAmount < 0) {
            return Mono.error(new IllegalArgumentException("BAD_REQUEST", "Debit amount cannot be negative"));
        }

        return getData(BALANCE_KEY)
                .flatMap(currentBalance -> {
                    int current = currentBalance != null ? currentBalance : 0;
                    int newBalance = current - debitAmount;
                    // Создаем запись о транзакции
                    PaymentTransaction transaction = PaymentTransaction.builder()
                            .transactionId(UUID.randomUUID().toString())
                            .success(false)
                            .balance(newBalance)
                            .build();
                    if (newBalance < 0) {
                        transaction.setMessage("Недостаточно средств на балансе");
                        return Mono.just(transaction);

                    } else {
                        transaction.setSuccess(true);
                        return saveData(BALANCE_KEY, newBalance)
                                .then(Mono.just(transaction));
                    }
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Error payment process", e)));
    }
}
