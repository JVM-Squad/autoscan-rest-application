package com.jongsoft.finance.rest.account;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.account.Account;
import com.jongsoft.finance.domain.account.SavingGoal;
import com.jongsoft.finance.providers.AccountProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.rest.model.AccountResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.control.Optional;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.function.Consumer;

@Controller("/api/accounts/{accountId}")
@Tag(name = "Account information")
@Secured(SecurityRule.IS_AUTHENTICATED)
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AccountEditResource {

    private final CurrentUserProvider currentUserProvider;
    private final AccountProvider accountProvider;

    @Get
    @Operation(
            summary = "Get Account",
            description = "Attempts to get the account with matching account id. If no account is found or you are not" +
                    "authorized an exception will be returned.",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(responseCode = "200", description = "The resulting account",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "401", description = "The account cannot be accessed"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    Mono<AccountResponse> get(@PathVariable long accountId) {
        return Mono.create(emitter -> {
            accountProvider.lookup(accountId)
                    .map(AccountResponse::new)
                    .ifPresent((Consumer<AccountResponse>) emitter::success)
                    .elseRun(() -> emitter.error(StatusException.notFound("Account not found")));
        });
    }

    @Post
    @Operation(
            summary = "Update Account",
            description = "Update an existing account with the new details provided in the body. The updated account will" +
                    " be returned, or if no account is found an exception.",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(responseCode = "200", description = "The updated account",
                            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
                    @ApiResponse(responseCode = "401", description = "The account cannot be accessed"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    Mono<AccountResponse> update(
            @PathVariable long accountId,
            @Valid @Body AccountEditRequest accountEditRequest) {
        return Mono.create(emitter -> {
            var account = accountProvider.lookup(accountId)
                    .getOrThrow(() -> StatusException.notFound("No account found with id " + accountId));

            account.rename(
                    accountEditRequest.getName(),
                    accountEditRequest.getDescription(),
                    accountEditRequest.getCurrency(),
                    accountEditRequest.getType());

            account.changeAccount(
                    accountEditRequest.getIban(),
                    accountEditRequest.getBic(),
                    accountEditRequest.getNumber());

            if (accountEditRequest.getInterestPeriodicity() != null) {
                account.interest(accountEditRequest.getInterest(), accountEditRequest.getInterestPeriodicity());
            }

            emitter.success(new AccountResponse(account));
        });
    }

    @Post(value = "/image")
    @Operation(
            summary = "Attach icon",
            description = "Attach an icon to the account. If any icon was previously registered it will be removed " +
                    "from the system.",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class))
    )
    Mono<AccountResponse> persistImage(
            @PathVariable long accountId,
            @Body @Valid AccountImageRequest imageRequest) {
        return Mono.create(emitter -> {
            var accountPromise = accountProvider.lookup(accountId);

            if (accountPromise.isPresent()) {
                accountPromise.get()
                        .registerIcon(imageRequest.getFileCode());

                emitter.success(new AccountResponse(accountPromise.get()));
            } else {
                emitter.error(StatusException.notFound("Could not find account"));
            }
        });
    }

    @Delete
    @Operation(
            summary = "Delete Account",
            parameters = @Parameter(
                    name = "accountId",
                    description = "The unique account id",
                    in = ParameterIn.PATH,
                    schema = @Schema(implementation = Long.class)),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Account successfully deleted"),
                    @ApiResponse(responseCode = "404", description = "No account can be located")
            }
    )
    void delete(@PathVariable long accountId) {
        accountProvider.lookup(accountId)
                .ifPresent(Account::terminate)
                .elseThrow(() -> StatusException.notFound("No account found for id " + accountId));
    }

    @Post("/savings")
    @Operation(
            operationId = "addSavingGoal",
            summary = "Create saving goal",
            description = "Creates a saving goal into the account, only valid for accounts of types SAVINGS and JOINED_SAVINGS"
    )
    @ApiDefaults
    AccountResponse createSavingGoal(
            @PathVariable long accountId,
            @Body @Valid AccountSavingGoalCreateRequest request) {
        accountProvider.lookup(accountId)
                .ifPresent(account ->
                        account.createSavingGoal(
                                request.getName(),
                                request.getGoal(),
                                request.getTargetDate()))
                .elseThrow(() -> StatusException.notFound("No account found for id " + accountId));

        return accountProvider.lookup(accountId)
                .map(AccountResponse::new)
                .get();
    }

    @Post("/savings/{savingId}")
    @Operation(
            operationId = "updateSavingGoal",
            summary = "Adjust Saving Goal",
            description = "Adjust a saving goal already attached to the savings account."
    )
    @ApiDefaults
    AccountResponse adjustSavingGoal(
            @PathVariable long accountId,
            @PathVariable long savingId,
            @Body @Valid AccountSavingGoalCreateRequest request) {
        accountProvider.lookup(accountId)
                .ifPresent(account ->
                        account.getSavingGoals()
                                .filter(goal -> goal.getId() == savingId)
                                .head()
                                .adjustGoal(request.getGoal(), request.getTargetDate()))
                .elseThrow(() -> StatusException.notFound("No account found for id " + accountId));

        return accountProvider.lookup(accountId)
                .map(AccountResponse::new)
                .get();
    }

    @Put("/savings/{savingId}/reserve")
    @Operation(
            operationId = "reserveForSavingGoal",
            summary = "Reserve Saving Goal",
            description = "Reserve money from the account towards the saving goal."
    )
    @ApiDefaults
    AccountResponse reservationForSavingGoal(
            @PathVariable long accountId,
            @PathVariable long savingId,
            @Valid @QueryValue @Positive BigDecimal amount) {
        accountProvider.lookup(accountId)
                .ifPresent(account ->
                        account.getSavingGoals()
                                .filter(goal -> goal.getId() == savingId)
                                .head()
                                .registerPayment(amount))
                .elseThrow(() -> StatusException.notFound("No account found for id " + accountId));

        return accountProvider.lookup(accountId)
                .map(AccountResponse::new)
                .get();
    }

    @Delete("/savings/{savingId}")
    @Operation(
            operationId = "deleteSavingGoal",
            summary = "Delete saving goal",
            description = "Removes a saving goal from the account."
    )
    @ApiDefaults
    void deleteSavingGoal(@PathVariable long accountId, @PathVariable long savingId) {
        accountProvider.lookup(accountId)
                .ifPresent(account ->
                        account.getSavingGoals()
                                .filter(goal -> goal.getId() == savingId)
                                .forEach(SavingGoal::completed))
                .elseThrow(() -> StatusException.notFound("No account found for id " + accountId));
    }

    private Optional<Account> getOrFail(MonoSink<HttpResponse<AccountResponse>> emitter, long accountId) {
        var accountOption = accountProvider.lookup(accountId);

        if (!accountOption.isPresent()) {
            emitter.success(HttpResponse.notFound());
            return Control.Option();
        } else if (!accountOption.get().getUser().getId().equals(currentUserProvider.currentUser().getId())) {
            emitter.success(HttpResponse.unauthorized());
            return Control.Option();
        }

        return accountOption;
    }

}
