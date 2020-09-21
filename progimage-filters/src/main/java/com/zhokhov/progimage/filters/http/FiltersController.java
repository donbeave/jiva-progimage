package com.zhokhov.progimage.filters.http;

import com.zhokhov.progimage.shared.ImageType;
import com.zhokhov.progimage.util.TikaUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.apache.tika.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Controller("/filters")
public class FiltersController {

    private final Logger LOG = LoggerFactory.getLogger(FiltersController.class);

    @Post("/black-and-white")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ExecuteOn(TaskExecutors.IO)
    public StreamedFile blackAndWhite(@NonNull CompletedFileUpload file)
            throws IOException {
        LOG.debug("Creating black-and-white image: {}", file.getFilename());

        return processFilter(file, BufferedImage.TYPE_BYTE_BINARY);
    }

    @Post("/grayscale")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ExecuteOn(TaskExecutors.IO)
    public StreamedFile grayscale(@NonNull CompletedFileUpload file)
            throws IOException {
        LOG.debug("Creating grayscale image: {}", file.getFilename());

        return processFilter(file, BufferedImage.TYPE_BYTE_GRAY);
    }

    private StreamedFile processFilter(CompletedFileUpload file,
                                       int filterType) throws IOException {
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

        BufferedImage blackAndWhiteImg =
                new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), filterType);

        blackAndWhiteImg
                .createGraphics()
                .drawImage(originalImage, 0, 0, null);

        Path blackAndWhiteImagePath = Files.createTempFile("filter", null);

        try (OutputStream os = Files.newOutputStream(blackAndWhiteImagePath)) {
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                ImageIO.write(blackAndWhiteImg, imageType.get().getFormatName(), ios);
            }
        }

        InputStream inputStream = Files.newInputStream(blackAndWhiteImagePath, StandardOpenOption.READ);
        return new StreamedFile(inputStream, imageType.get().getMediaType());
    }

}
