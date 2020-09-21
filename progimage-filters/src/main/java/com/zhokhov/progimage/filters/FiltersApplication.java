package com.zhokhov.progimage.filters;

import io.micronaut.runtime.Micronaut;

import java.time.ZoneOffset;
import java.util.TimeZone;

public class FiltersApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));

        Micronaut.run(FiltersApplication.class, args);
    }

}
