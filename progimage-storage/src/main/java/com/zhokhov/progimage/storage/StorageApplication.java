package com.zhokhov.progimage.storage;

import io.micronaut.runtime.Micronaut;

import java.time.ZoneOffset;
import java.util.TimeZone;

public class StorageApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));

        Micronaut.run(StorageApplication.class, args);
    }

}
