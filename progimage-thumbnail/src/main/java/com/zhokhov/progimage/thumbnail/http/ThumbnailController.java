package com.zhokhov.progimage.thumbnail.http;

import com.zhokhov.progimage.shared.ImageType;
import com.zhokhov.progimage.util.TikaUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.apache.tika.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Controller("/thumbnail")
public class ThumbnailController {

    private final Logger LOG = LoggerFactory.getLogger(ThumbnailController.class);

    @Post("/scale")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ExecuteOn(TaskExecutors.IO)
    public StreamedFile scale(@NonNull CompletedFileUpload file, @QueryValue int width, @QueryValue int height)
            throws IOException {
        LOG.debug("Creating thumbnail image: {}", file.getFilename());

        Optional<MimeType> mimeType = TikaUtils.detectMimeType(
                file.getInputStream(),
                file.getContentType().map(MediaType::getName).orElse(null),
                file.getFilename()
        );

        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        if (mimeType.isEmpty()) {
            throw new UnsupportedFormatException();
        }

        Optional<ImageType> imageType = ImageType.findByExtension(mimeType.get().getExtension());

        if (imageType.isEmpty()) {
            throw new UnsupportedFormatException();
        }

        BufferedImage thumbnailImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        thumbnailImage
                .createGraphics()
                .drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

        Path thumbnailImagePath = Files.createTempFile("thumbnail", null);

        try (OutputStream os = Files.newOutputStream(thumbnailImagePath)) {
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                ImageIO.write(thumbnailImage, imageType.get().getFormatName(), ios);
            }
        }

        InputStream inputStream = Files.newInputStream(thumbnailImagePath, StandardOpenOption.READ);

        return new StreamedFile(inputStream, imageType.get().getMediaType());
    }

}
