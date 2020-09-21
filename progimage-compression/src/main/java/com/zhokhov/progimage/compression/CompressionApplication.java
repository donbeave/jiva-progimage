package com.zhokhov.progimage.compression;

import io.micronaut.runtime.Micronaut;

import java.time.ZoneOffset;
import java.util.TimeZone;

public class CompressionApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));

        Micronaut.run(CompressionApplication.class, args);
    }

}
