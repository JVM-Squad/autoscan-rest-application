package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.SavingGoal;
import com.jongsoft.finance.schedule.Periodicity;
import com.jongsoft.finance.schedule.Schedulable;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Introspected
public class AccountResponse {

    private final Account wrapped;

    public AccountResponse(final Account wrapped) {
        Objects.requireNonNull(wrapped, "Account cannot be null for JSON response.");
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String getName() {
        return wrapped.getName();
    }

    public String getDescription() {
        return wrapped.getDescription();
    }

    public String getType() {
        return wrapped.getType();
    }

    public String getIconFileCode() {
        return wrapped.getImageFileToken();
    }

    public NumberInformation getAccount() {
        return new NumberInformation();
    }

    public InterestInformation getInterest() {
        return new InterestInformation();
    }

    public History getHistory() {
        return new History();
    }

    public Set<SavingGoalResponse> getSavingGoals() {
        if (wrapped.getSavingGoals() != null) {
            return wrapped.getSavingGoals()
                    .map(SavingGoalResponse::new)
                    .toJava();
        }

        return null;
    }

    @Introspected
    @RequiredArgsConstructor
    public class SavingGoalResponse {

        private final SavingGoal wrapped;

        public long getId() {
            return wrapped.getId();
        }

        public String getName() {
            return wrapped.getName();
        }

        public String getDescription() {
            return wrapped.getDescription();
        }

        public ScheduleResponse getSchedule() {
            if (wrapped.getSchedule() != null) {
                return new ScheduleResponse(wrapped.getSchedule());
            }

            return null;
        }

        public BigDecimal getGoal() {
            return wrapped.getGoal();
        }

        public BigDecimal getReserved() {
            return wrapped.getAllocated();
        }

        public BigDecimal getInstallments() {
            if (wrapped.getSchedule() != null) {
                return wrapped.computeAllocation();
            }

            return null;
        }

        public LocalDate getTargetDate() {
            return wrapped.getTargetDate();
        }

        public long getMonthsLeft() {
            var monthsLeft = ChronoUnit.MONTHS.between(LocalDate.now(), wrapped.getTargetDate());
            return Math.max(0, monthsLeft);
        }

    }

    @Introspected
    public class InterestInformation {

        public Periodicity getPeriodicity() {
            return wrapped.getInterestPeriodicity();
        }

        public double getInterest() {
            return wrapped.getInterest();
        }

    }

    @Introspected
    public class NumberInformation {

        public String getIban() {
            return wrapped.getIban();
        }

        public String getBic() {
            return wrapped.getBic();
        }

        public String getNumber() {
            return wrapped.getNumber();
        }

        public String getCurrency() {
            return wrapped.getCurrency();
        }
    }

    @Introspected
    public class History {

        public LocalDate getFirstTransaction() {
            return wrapped.getFirstTransaction();
        }

        public LocalDate getLastTransaction() {
            return wrapped.getLastTransaction();
        }

    }
}
