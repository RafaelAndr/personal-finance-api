package com.personal_finance.service;

import com.personal_finance.dto.payment.PaymentResponseDto;
import com.personal_finance.entity.Payment;
import com.personal_finance.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void saveExpensePayment(Payment payment){
        paymentRepository.save(payment);
    }

    public PaymentResponseDto getPayment(UUID id){
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        UUID accountId = payment.getAccount() != null
                ? payment.getAccount().getId()
                : null;

        return new PaymentResponseDto(
                payment.getId(),
                payment.getPaymentMethod(),
                payment.getExpense().getId(),
                payment.getUser().getId(),
                accountId
        );
    }

    public List<PaymentResponseDto> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();

        return payments.stream()
                .map(payment -> {
                    UUID accountId = payment.getAccount() != null
                            ? payment.getAccount().getId()
                            : null;

                    return new PaymentResponseDto(
                            payment.getId(),
                            payment.getPaymentMethod(),
                            payment.getExpense().getId(),
                            payment.getUser().getId(),
                            accountId
                    );
                })
                .toList();
    }
}
