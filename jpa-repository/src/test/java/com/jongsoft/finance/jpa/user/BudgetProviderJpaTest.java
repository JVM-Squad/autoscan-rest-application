package com.jongsoft.finance.jpa.user;

import java.io.IOException;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.finance.domain.user.BudgetProvider;
import com.jongsoft.finance.jpa.JpaTestSetup;

import io.micronaut.test.annotation.MockBean;

class BudgetProviderJpaTest extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private BudgetProvider budgetProvider;

    void setup() throws IOException {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/user/budget-provider.sql"
        );
    }

    @Test
    void lookup() throws IOException {
        setup();
        var check = budgetProvider.lookup();
        Assertions.assertThat(check).hasSize(2);
    }

    @Test
    void lookup_201901() throws IOException {
        setup();
        var check = budgetProvider.lookup(2019, 1);

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getExpenses()).hasSize(2);
        Assertions.assertThat(check.get().getExpectedIncome()).isEqualTo(2500);
    }

    @Test
    void lookup_202001() throws IOException {
        setup();
        var check = budgetProvider.lookup(2020, 1);

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getExpenses()).hasSize(2);
        Assertions.assertThat(check.get().getExpectedIncome()).isEqualTo(2800);
    }

    @Test
    void first() throws IOException {
        setup();
        var check = budgetProvider.first();

        Assertions.assertThat(check.isPresent()).isTrue();
        Assertions.assertThat(check.get().getExpenses()).hasSize(2);
        Assertions.assertThat(check.get().getExpectedIncome()).isEqualTo(2500);
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
