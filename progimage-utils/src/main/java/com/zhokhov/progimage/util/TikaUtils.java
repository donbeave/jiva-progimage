package com.zhokhov.progimage.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.util.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public final class TikaUtils {

    private static final TikaConfig TIKA_CONFIG = TikaConfig.getDefaultConfig();
    private static final Tika TIKA = new Tika(TIKA_CONFIG);

    private TikaUtils() {
    }

    public static Optional<MimeType> detectMimeType(@NonNull byte[] bytes) throws IOException {
        String contentType = TIKA.detect(bytes);
        return getMimeType(contentType);
    }

    public static Optional<MimeType> detectMimeType(@NonNull InputStream inputStream,
                                                    @Nullable String presumedContentType,
                                                    @Nullable String filename) throws IOException {
        Metadata metadata = new Metadata();
        if (!StringUtils.isEmpty(presumedContentType)) {
            metadata.set(Metadata.CONTENT_TYPE, presumedContentType);
        }
        if (!StringUtils.isEmpty(filename)) {
            metadata.set(Metadata.RESOURCE_NAME_KEY, filename);
        }
        String contentType = TIKA.detect(inputStream, metadata);
        return getMimeType(contentType);
    }

    public static Optional<MimeType> detectMimeType(@NonNull Path path) throws IOException {
        String contentType = TIKA.detect(path);
        return getMimeType(contentType);
    }

    private static Optional<MimeType> getMimeType(String contentType) {
        try {
            MimeType mimeType = TIKA_CONFIG.getMimeRepository().forName(contentType);
            String extension = mimeType.getExtension();

            if (StringUtils.isEmpty(extension)) {
                return Optional.empty();
            }

            return Optional.of(mimeType);
        } catch (MimeTypeException e) {
            return Optional.empty();
        }
    }

}
