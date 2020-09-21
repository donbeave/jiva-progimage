package com.zhokhov.progimage.compression.http;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;

import javax.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {UnsupportedFormatException.class, ExceptionHandler.class})
public class UnsupportedFormatExceptionHandler implements ExceptionHandler<UnsupportedFormatException, HttpResponse> {

    @Override
    public HttpResponse handle(HttpRequest request, UnsupportedFormatException exception) {
        return HttpResponse.unprocessableEntity();
    }

}
