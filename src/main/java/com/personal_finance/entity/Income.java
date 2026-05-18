package com.personal_finance.entity;

import com.personal_finance.entity.enums.IncomeCategory;
import com.personal_finance.exception.AccessForbiddenException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "income_category")
    @Enumerated(EnumType.STRING)
    private IncomeCategory incomeCategory;

    @Column(name = "income_value")
    private BigDecimal value;

    @Column(name = "receipt_date")
    private LocalDate receiptDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    public void validateOwnership(UUID userId){
        if (this.user == null){
            throw new EntityNotFoundException("Income has no user associated");
        }

        if (!this.user.getId().equals(userId)){
            throw new AccessForbiddenException("You can't access this income");
        }
    }
}