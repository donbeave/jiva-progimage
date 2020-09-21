package com.zhokhov.progimage.storage.http;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;

import javax.inject.Singleton;
import java.io.FileNotFoundException;

@Produces
@Singleton
@Requires(classes = {FileNotFoundException.class, ExceptionHandler.class})
public class FileNotFoundExceptionHandler implements ExceptionHandler<FileNotFoundException, HttpResponse> {

    @Override
    public HttpResponse handle(HttpRequest request, FileNotFoundException exception) {
        return HttpResponse.notFound();
    }

}
