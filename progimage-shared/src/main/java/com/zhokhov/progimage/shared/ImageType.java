package com.zhokhov.progimage.shared;

import io.micronaut.http.MediaType;

import java.util.Arrays;
import java.util.Optional;

/**
 * Collection of supported image types.
 */
public enum ImageType {

    JPEG(".jpg", "jpg", 1, MediaType.IMAGE_JPEG_TYPE),
    PNG(".png", "png", 2, MediaType.IMAGE_PNG_TYPE),
    GIF(".gif", "gif", 3, MediaType.IMAGE_GIF_TYPE),
    BMP(".bmp", "bmp", 4, MediaType.of("image/bmp"));

    private final String extension;
    private final String formatName;
    private final int formatNumber;
    private final MediaType mediaType;

    ImageType(String extension, String formatName, int formatNumber, MediaType mediaType) {
        this.extension = extension;
        this.formatName = formatName;
        this.formatNumber = formatNumber;
        this.mediaType = mediaType;
    }

    public String getExtension() {
        return extension;
    }

    public String getFormatName() {
        return formatName;
    }

    public int getFormatNumber() {
        return formatNumber;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public static Optional<ImageType> findByFormatNumber(int formatNumber) {
        return Arrays.stream(ImageType.values())
                .filter(it -> it.getFormatNumber() == formatNumber)
                .findFirst();
    }

    public static Optional<ImageType> findByExtension(String extension) {
        return Arrays.stream(ImageType.values())
                .filter(it -> it.getExtension().equalsIgnoreCase(extension))
                .findFirst();
    }

}
