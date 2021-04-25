package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.jpa.core.FilterCommandJpa;
import com.jongsoft.finance.providers.TransactionScheduleProvider;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.data.model.Sort;

import java.time.LocalDate;

public class ScheduleFilterCommand extends FilterCommandJpa implements TransactionScheduleProvider.FilterCommand {

    @Override
    public TransactionScheduleProvider.FilterCommand contract(Sequence<EntityRef> contracts) {
        if (!contracts.isEmpty()) {
            hql("contracts", " and t.contract.id in (:contracts)");
            parameter("contracts", contracts.map(EntityRef::getId).toJava());
        }
        return this;
    }

    @Override
    public TransactionScheduleProvider.FilterCommand activeOnly() {
        hql("dates", " and (end is null or end > :now)");
        parameter("now", LocalDate.now());
        return this;
    }

    @Override
    protected String fromHql() {
        return "from ScheduledTransactionJpa t where 1 = 1";
    }

    @Override
    public Sort sort() {
        return Sort.of(Sort.Order.desc("t.start"));
    }

    @Override
    public int page() {
        return 0;
    }

    @Override
    public int pageSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public FilterCommandJpa user(String username) {
        hql("username", " and t.user.username = :username");
        parameter("username", username);
        return this;
    }

}
