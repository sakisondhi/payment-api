package com.example.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    PaymentRepository paymentRepository;

    @InjectMocks
    PaymentService paymentService;

    @Test 
    void createsPaymentwithPendingStatus() {
        when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

        CreatePaymentRequest request =
                new CreatePaymentRequest(new BigDecimal("25.00"), "USD");

        Payment result = paymentService.createPayment(request);

        assertEquals(PaymentStatus.PENDING, result.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test 
    void getPaymentByIdReturnsPayment() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(paymentId, new BigDecimal("50.00"), "USD", PaymentStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        Payment result = paymentService.getPaymentById(paymentId);

        assertEquals(payment, result);
        verify(paymentRepository).findById(paymentId);
    }

    @Test 
    void getPaymentByIdThrowsExceptionWhenNotFound() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(paymentId));
        verify(paymentRepository).findById(paymentId);
    }

    @Test 
    void getAllPaymentsFiltersByStatus() {
        Payment payment1 = new Payment(UUID.randomUUID(), new BigDecimal("10.00"), "INR", PaymentStatus.PENDING);
        Payment payment2 = new Payment(UUID.randomUUID(), new BigDecimal("20.00"), "USD", PaymentStatus.SUCCESS);
        when(paymentRepository.findAll()).thenReturn(java.util.List.of(payment1, payment2));

        java.util.List<Payment> result = paymentService.getAllPayments(Optional.of(PaymentStatus.PENDING));

        assertEquals(1, result.size());
        assertEquals(PaymentStatus.PENDING, result.get(0).getStatus());
    }
}
