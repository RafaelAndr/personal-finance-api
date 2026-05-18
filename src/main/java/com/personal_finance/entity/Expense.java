package com.personal_finance.entity;

import com.personal_finance.entity.enums.ExpenseCategory;
import com.personal_finance.exception.AccessForbiddenException;
import com.personal_finance.exception.ExpenseAlreadyPaidException;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "description")
    private String description;

    @Column(name = "expense_value")
    private BigDecimal value;

    @Column(name = "paid")
    private boolean paid;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToOne(mappedBy = "expense")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "expense_category")
    private ExpenseCategory expenseCategory;

    public void validateOwnership(UUID userId){
        if (this.user == null){
            throw new EntityNotFoundException("Expense has no user associated");
        }

        if (!this.user.getId().equals(userId)){
            throw new AccessForbiddenException("You can't access this expense");
        }
    }

    public void markAsPaid(){
        if (this.paid){
            throw new ExpenseAlreadyPaidException("Expense is already paid");
        }
        this.paid = true;
    }
}
