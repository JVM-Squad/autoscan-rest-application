package com.jongsoft.finance.jpa.transaction.entity;

import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.schedule.Periodicity;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "transaction_schedule")
public class ScheduledTransactionJpa extends EntityJpa {

    @Column(name = "start_date", columnDefinition = "DATE")
    private LocalDate start;
    @Column(name = "end_date", columnDefinition = "DATE")
    private LocalDate end;

    private double amount;
    private String name;
    private String description;

    @Enumerated(value = EnumType.STRING)
    private Periodicity periodicity;

    @Column(name = "reoccur")
    private int interval;

    @ManyToOne
    private UserAccountJpa user;

    @ManyToOne
    private AccountJpa source;

    @ManyToOne
    private AccountJpa destination;

    @Builder
    public ScheduledTransactionJpa(
            LocalDate start,
            LocalDate end,
            double amount,
            String name,
            String description,
            Periodicity periodicity,
            int interval,
            UserAccountJpa user,
            AccountJpa source,
            AccountJpa destination) {
        this.start = start;
        this.end = end;
        this.amount = amount;
        this.name = name;
        this.description = description;
        this.periodicity = periodicity;
        this.interval = interval;
        this.user = user;
        this.source = source;
        this.destination = destination;
    }

    public ScheduledTransactionJpa() {
    }
}
