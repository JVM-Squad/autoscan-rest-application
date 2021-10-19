package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.jpa.JpaTestSetup;
import com.jongsoft.finance.providers.ContractProvider;
import com.jongsoft.finance.security.AuthenticationFacade;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.time.LocalDate;

class ContractProviderJpaIT extends JpaTestSetup {

    @Inject
    private AuthenticationFacade authenticationFacade;

    @Inject
    private ContractProvider contractProvider;

    void setup() {
        Mockito.when(authenticationFacade.authenticated()).thenReturn("demo-user");
        loadDataset(
                "sql/base-setup.sql",
                "sql/account/contract-provider.sql"
        );
    }

    @Test
    void lookup() {
        setup();
        var check = contractProvider.lookup();

        Assertions.assertThat(check).hasSize(1);
        Assertions.assertThat(check.head().getId()).isEqualTo(1L);
    }

    @Test
    void lookup_name() {
        setup();

        var check = contractProvider.lookup("Test contract")
                .block();

        Assertions.assertThat(check.getId()).isEqualTo(1L);
        Assertions.assertThat(check.getName()).isEqualTo("Test contract");
        Assertions.assertThat(check.getStartDate()).isEqualTo(LocalDate.of(2019, 2, 1));
        Assertions.assertThat(check.getEndDate()).isEqualTo(LocalDate.of(2020, 2, 1));
    }

    @Test
    void lookup_nameIncorrectUser() {
        setup();

        Assertions.assertThat(contractProvider.lookup("In between").blockOptional())
                .isEmpty();
    }

    @Test
    void search() {
        setup();

        StepVerifier.create(contractProvider.search("conT"))
                .expectNext(Contract.builder().id(1L).build())
                .verifyComplete();
    }

    @Test
    void search_incorrectUser() {
        setup();

        StepVerifier.create(contractProvider.search("betwe"))
                .verifyComplete();
    }

    @MockBean
    AuthenticationFacade authenticationFacade() {
        return Mockito.mock(AuthenticationFacade.class);
    }
}
