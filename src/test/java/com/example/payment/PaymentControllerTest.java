package com.example.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void createsPayment() throws Exception {
        Payment payment = new Payment(
                UUID.randomUUID(), new BigDecimal("25.00"), "USD", PaymentStatus.PENDING);
        when(paymentService.createPayment(any(CreatePaymentRequest.class))).thenReturn(payment);

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 25.00, "currency": "USD"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.amount").value(25.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void rejectsInvalidPayment() throws Exception {
        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 0, "currency": "USD"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void returnsNotFoundWhenPaymentDoesNotExist() throws Exception {
        UUID paymentId = UUID.randomUUID();
        when(paymentService.getPaymentById(paymentId))
                .thenThrow(new PaymentNotFoundException("Payment not found with id: " + paymentId));

        mockMvc.perform(get("/payments/{id}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment not found with id: " + paymentId));
    }

    @Test 
    void returnsPaymentById() throws Exception {
        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment(paymentId, new BigDecimal("50.00"), "USD", PaymentStatus.PENDING);
        when(paymentService.getPaymentById(paymentId)).thenReturn(payment);

        mockMvc.perform(get("/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId().toString()))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test 
    void returnsAllPayments() throws Exception {
        Payment payment1 = new Payment(UUID.randomUUID(), new BigDecimal("25.00"), "USD", PaymentStatus.PENDING);
        Payment payment2 = new Payment(UUID.randomUUID(), new BigDecimal("50.00"), "EUR", PaymentStatus.SUCCESS);
        when(paymentService.getAllPayments(any())).thenReturn(java.util.List.of(payment1, payment2));

        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(payment1.getId().toString()))
                .andExpect(jsonPath("$[0].amount").value(25.00))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(payment2.getId().toString()))
                .andExpect(jsonPath("$[1].amount").value(50.00))
                .andExpect(jsonPath("$[1].currency").value("EUR"))
                .andExpect(jsonPath("$[1].status").value("SUCCESS"));
    }

    @Test 
    void returnsPaymentsFilteredByStatus() throws Exception {
        Payment payment1 = new Payment(UUID.randomUUID(), new BigDecimal("25.00"), "USD", PaymentStatus.SUCCESS);
        when(paymentService.getAllPayments(java.util.Optional.of(PaymentStatus.SUCCESS)))
                .thenReturn(java.util.List.of(payment1));

        mockMvc.perform(get("/payments").param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(payment1.getId().toString()))
                .andExpect(jsonPath("$[0].amount").value(25.00))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }
}
