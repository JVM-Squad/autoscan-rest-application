package com.jongsoft.finance.rest.model;

import com.jongsoft.finance.domain.user.Budget;

public class ExpenseResponse {

    private final Budget.Expense wrapped;

    public ExpenseResponse(Budget.Expense wrapped) {
        this.wrapped = wrapped;
    }

    public long getId() {
        return wrapped.getId();
    }

    public String name() {
        return wrapped.getName();
    }

    public double getExpected() {
        return wrapped.computeBudget();
    }

    public Bounds getBounds() {
        return new Bounds();
    }

    public class Bounds {

        public double getLower() {
            return wrapped.getLowerBound();
        }

        public double getUpper() {
            return wrapped.getUpperBound();
        }
    }
}
