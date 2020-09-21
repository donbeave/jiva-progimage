package com.zhokhov.progimage.util;

import org.apache.tika.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class TestFileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestFileUtils.class);

    private TestFileUtils() {
    }

    public static void saveBytesToTempFile(byte[] bytes) {
        try {
            Optional<MimeType> mimeType = TikaUtils.detectMimeType(bytes);

            Path filePath = Files.createTempFile(
                    "test-file",
                    mimeType.map(MimeType::getExtension).orElse(null)
            );

            try (OutputStream fos = Files.newOutputStream(filePath)) {
                fos.write(bytes);
            }

            LOG.info("Preview file saved: " + filePath.toUri());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
