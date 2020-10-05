package com.jongsoft.finance.filter;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Slf4j
@Produces
@Singleton
@Requires(classes = {StatusException.class, ExceptionHandler.class})
public class StatusExceptionHandler implements ExceptionHandler<StatusException, HttpResponse<JsonError>> {

    @Override
    public HttpResponse<JsonError> handle(HttpRequest request, StatusException exception) {
        log.debug("[{}] - Resource requested resolved in issues {} with message '{}'",
                request.getPath(),
                exception.getStatusCode(),
                exception.getMessage());

        var error = new JsonError(exception.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse
                .status(HttpStatus.valueOf(exception.getStatusCode()))
                .body(error);
    }

}
