package com.zhokhov.progimage.rotation;

import io.micronaut.runtime.Micronaut;

import java.time.ZoneOffset;
import java.util.TimeZone;

public class RotationApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));

        Micronaut.run(RotationApplication.class, args);
    }

}
