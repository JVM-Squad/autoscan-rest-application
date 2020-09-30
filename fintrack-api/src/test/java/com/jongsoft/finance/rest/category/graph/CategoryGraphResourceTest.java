package com.jongsoft.finance.rest.category.graph;

import com.jongsoft.finance.core.date.DateRange;
import com.jongsoft.finance.core.date.Dates;
import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.EntityRef;
import com.jongsoft.finance.domain.transaction.TransactionProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.domain.user.CategoryProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.lang.API;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Locale;

class CategoryGraphResourceTest extends TestSetup {

    private CategoryGraphResource subject;

    private FilterFactory filterFactory;
    @Mock
    private CategoryProvider categoryProvider;
    @Mock
    private TransactionProvider transactionProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();
        subject = new CategoryGraphResource(
                new ResourceBundleMessageSource("i18n.messages"),
                filterFactory,
                categoryProvider,
                transactionProvider);
    }

    @Test
    void graph() {
        final Category category = Category.builder()
                .id(1L)
                .label("grocery")
                .description("For groceries")
                .lastActivity(LocalDate.of(2019, 1, 2))
                .build();

        Mockito.when(categoryProvider.lookup()).thenReturn(API.List(category));
        Mockito.when(transactionProvider.balance(Mockito.any())).thenReturn(API.Option());

        subject.graph(Dates.startOfMonth(2019, 1), Dates.endOfMonth(2019, 1), Locale.GERMAN);

        var mockFilter = filterFactory.transaction();
        Mockito.verify(mockFilter).onlyIncome(false);
        Mockito.verify(mockFilter).ownAccounts();
        Mockito.verify(mockFilter).range(DateRange.forMonth(2019, 1));
        Mockito.verify(mockFilter).categories(API.List(new EntityRef(category.getId())));
    }
}