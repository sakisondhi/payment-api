package com.example.payment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

@Service 
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment(UUID.randomUUID(), request.amount()
        , request.currency(), PaymentStatus.PENDING);
        // Set payment properties based on the request
        return paymentRepository.save(payment);
    }

    @SuppressWarnings("null")
    public List<Payment> getAllPayments(Optional<PaymentStatus> paymentStatus) {
        Stream<Payment> payments = paymentRepository.findAll().stream();
        if (paymentStatus.isPresent()) {
            PaymentStatus requestedStatus = paymentStatus.get();
            payments = payments.filter(payment -> payment.getStatus() == requestedStatus);
        }
        
        return payments.sorted(Comparator.comparing(Payment::getAmount)).toList();
    }

    public Payment getPaymentById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }
}
