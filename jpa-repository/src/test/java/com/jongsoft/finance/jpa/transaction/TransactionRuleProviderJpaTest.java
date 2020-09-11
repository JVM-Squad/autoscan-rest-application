package com.jongsoft.finance.jpa.transaction;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.transaction.TransactionRuleProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;

import io.micronaut.test.annotation.MockBean;

class TransactionRuleProviderJpaTest extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private TransactionRuleProvider ruleProvider;

    void setup() {
        Mockito.doReturn("demo-user").when(authenticationFacade).authenticated();
        loadDataset(
                "sql/base-setup.sql",
                "sql/transaction/rule-group-provider.sql",
                "sql/transaction/rule-provider.sql"
        );
    }

    @Test
    void lookup() {
        setup();
        var check = ruleProvider.lookup();
        Assertions.assertThat(check).hasSize(1);
    }

    @Test
    void lookup_group() {
        setup();
        var check = ruleProvider.lookup("Grocery stores");
        Assertions.assertThat(check).hasSize(1);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }

}
