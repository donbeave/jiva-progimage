package com.zhokhov.progimage.storage.http;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;

import javax.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {IncorrectImageIdException.class, ExceptionHandler.class})
public class IncorrectImageIdExceptionHandler implements ExceptionHandler<IncorrectImageIdException, HttpResponse> {

    @Override
    public HttpResponse handle(HttpRequest request, IncorrectImageIdException exception) {
        return HttpResponse.notFound();
    }

}
