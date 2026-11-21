package com.example.payment;

import java.math.BigDecimal;
import java.util.UUID;

public class Payment {
    private UUID id;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;

    public Payment(UUID id, BigDecimal amount, String currency, PaymentStatus status) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

}
