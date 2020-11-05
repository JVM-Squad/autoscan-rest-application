package com.jongsoft.finance.rest.security;

import com.jongsoft.finance.domain.FinTrack;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserProvider;
import com.jongsoft.finance.rest.ApiDefaults;
import com.jongsoft.finance.security.PasswordEncoder;
import com.jongsoft.lang.API;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.http.*;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.event.LoginSuccessfulEvent;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureConfiguration;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;

import javax.validation.Valid;
import java.util.Map;

@Tag(name = "Authentication")
@Controller(consumes = MediaType.APPLICATION_JSON)
public class AuthenticationResource {

    private final AccessRefreshTokenGenerator accessRefreshTokenGenerator;
    private final AuthenticationProvider authenticationProvider;
    private final RSASignatureConfiguration rsaSignatureConfiguration;
    private final ApplicationEventPublisher eventPublisher;
    private final UserProvider userProvider;

    private final PasswordEncoder passwordEncoder;
    private final ProcessEngine processEngine;

    public AuthenticationResource(
            final AccessRefreshTokenGenerator accessRefreshTokenGenerator,
            final AuthenticationProvider authenticationProvider,
            final RSASignatureConfiguration rsaSignatureConfiguration,
            final ApplicationEventPublisher eventPublisher,
            final UserProvider userProvider,
            final PasswordEncoder passwordEncoder,
            final ProcessEngine processEngine) {
        this.accessRefreshTokenGenerator = accessRefreshTokenGenerator;
        this.authenticationProvider = authenticationProvider;
        this.rsaSignatureConfiguration = rsaSignatureConfiguration;
        this.eventPublisher = eventPublisher;
        this.userProvider = userProvider;
        this.passwordEncoder = passwordEncoder;
        this.processEngine = processEngine;
    }

    @ApiDefaults
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post(value = "/api/security/authenticate")
    @Operation(
            summary = "Authenticate",
            description = "Authenticate against FinTrack to obtain a JWT token",
            operationId = "authenticate"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully authenticated",
            content = @Content(schema = @Schema(implementation = AccessRefreshToken.class)))
    public Single<MutableHttpResponse<?>> authenticate(
            HttpRequest<?> request,
            @Valid @Body AuthenticationRequest authenticationRequest) {
        var response = Flowable.fromPublisher(
                authenticationProvider.authenticate(request, authenticationRequest));

        return response.map(authenticated -> {
            if (authenticated.isAuthenticated() && authenticated.getUserDetails().isPresent()) {
                var userDetails = authenticated.getUserDetails().get();

                eventPublisher.publishEvent(new LoginSuccessfulEvent(userDetails));
                var refreshToken = accessRefreshTokenGenerator.generate(userDetails);
                if (refreshToken.isPresent()) {
                    var actualToken = refreshToken.get();
                    FinTrack.registerToken(
                            userDetails.getUsername(),
                            actualToken.getRefreshToken(),
                            actualToken.getExpiresIn());

                    return HttpResponse.ok(actualToken);
                }
            }

            return HttpResponse.unauthorized();
        }).first(HttpResponse.unauthorized());
    }

    @ApiDefaults
    @Status(HttpStatus.CREATED)
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Put("/api/security/create-account")
    @Operation(
            summary = "Create account",
            description = "Creates a new account",
            operationId = "createAccount"
    )
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(nullable = true)))
    public void createAccount(@Valid @Body AuthenticationRequest authenticationRequest) {
        processEngine.getRuntimeService()
                .startProcessInstanceByKey("RegisterUserAccount", Map.of(
                        "username", authenticationRequest.getIdentity(),
                        "passwordHash", passwordEncoder.encrypt(authenticationRequest.getSecret())));
    }

    @ApiDefaults
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/api/security/token-refresh")
    @Operation(
            summary = "Refresh authorization",
            description = "Renew the JWT token if it is about to expire",
            operationId = "refreshToken"
    )
    public Single<MutableHttpResponse<AccessRefreshToken>> refresh(@Body @Valid TokenRefreshRequest request) {
        return userProvider.refreshToken(request.getToken())
                .map(user -> {
                    var userDetails = new UserDetails(
                            user.getUsername(),
                            API.List(user.getRoles()).map(Role::getName).toJava());

                    return accessRefreshTokenGenerator.generate(request.getToken(), userDetails)
                            .stream()
                            .peek(token -> FinTrack.registerToken(
                                    user.getUsername(),
                                    token.getRefreshToken(),
                                    token.getExpiresIn()))
                            .map(HttpResponse::ok)
                            .findFirst()
                            .orElseGet(HttpResponse::unauthorized);
                })
                .switchIfEmpty(Single.just(HttpResponse.unauthorized()));
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get(value = "/.well-known/public-key")
    @Operation(hidden = true)
    public String publicKey() {
        return Base64.encodeBase64String(rsaSignatureConfiguration.getPublicKey().getEncoded());
    }

}
