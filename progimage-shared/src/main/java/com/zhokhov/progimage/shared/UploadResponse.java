package com.zhokhov.progimage.shared;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class UploadResponse {

    private String imageId;

    protected UploadResponse() {
    }

    public UploadResponse(String imageId) {
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }

    protected void setImageId(String imageId) {
        this.imageId = imageId;
    }

}
