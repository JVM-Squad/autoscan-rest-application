package com.jongsoft.finance.jpa.user;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.user.ChangePasswordCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class ChangePasswordHandler implements CommandHandler<ChangePasswordCommand> {

    private final ReactiveEntityManager entityManager;

    public ChangePasswordHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangePasswordCommand command) {
        log.trace("[{}] - Updating password for user", command.username());

        entityManager.update()
                .hql("""
                        update UserAccountJpa
                        set password = :password
                        where username = :username""")
                .set("username", command.username())
                .set("password", command.password())
                .execute();
    }

}
