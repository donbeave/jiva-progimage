package com.zhokhov.progimage.storage.repository;

import com.zhokhov.progimage.shared.ImageType;
import com.zhokhov.progimage.storage.http.UnsupportedFormatException;
import com.zhokhov.progimage.util.DigestUtils;
import com.zhokhov.progimage.util.TikaUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import org.apache.tika.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Singleton
public class ImageLocalStorageRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ImageLocalStorageRepository.class);

    @Value("${progimage.storage-dir}") String folderPath;
    @Value("${progimage.cache-dir}") String cacheFolderPath;

    @PostConstruct
    public void init() throws IOException {
        while (folderPath.endsWith("/")) {
            folderPath = folderPath.substring(0, folderPath.length() - 1);
        }

        while (cacheFolderPath.endsWith("/")) {
            cacheFolderPath = cacheFolderPath.substring(0, cacheFolderPath.length() - 1);
        }

        Files.createDirectories(Path.of(folderPath));
        Files.createDirectories(Path.of(cacheFolderPath));
    }

    public StreamedFile retrieveImage(@NonNull ImageId originalImageId, @Nullable ImageType outputImageType) throws IOException {
        requireNonNull(originalImageId, "originalImageId");

        Path originalImagePath = Paths.get(folderPath + "/" + originalImageId.toString());

        if (Files.notExists(originalImagePath)) {
            throw new FileNotFoundException(originalImageId.toString());
        }

        ImageType requestedImageType = Optional.ofNullable(outputImageType).orElse(originalImageId.getType());

        if (!originalImageId.getType().equals(requestedImageType)) {
            LOG.debug("Converting from {} to {}", originalImageId.getType(), requestedImageType);

            Path convertedImagePath = Paths.get(cacheFolderPath + "/" + originalImageId.toString() + requestedImageType.getExtension());

            if (Files.exists(convertedImagePath)) {
                LOG.debug("Serving cached version: {}", convertedImagePath);

                InputStream inputStream = Files.newInputStream(convertedImagePath, StandardOpenOption.READ);
                return new StreamedFile(inputStream, requestedImageType.getMediaType());
            }

            try (InputStream inputStream = Files.newInputStream(originalImagePath, StandardOpenOption.READ)) {
                LOG.debug("Cache formatted image: {}", convertedImagePath);

                ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
                BufferedImage image = ImageIO.read(imageInputStream);

                try (OutputStream os = Files.newOutputStream(convertedImagePath)) {
                    try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                        ImageIO.write(image, requestedImageType.getFormatName(), ios);
                    }
                }

                InputStream convertedInputStream = Files.newInputStream(convertedImagePath, StandardOpenOption.READ);
                return new StreamedFile(convertedInputStream, requestedImageType.getMediaType());
            }
        }

        LOG.debug("Serving original version: {}", originalImagePath);

        InputStream inputStream = Files.newInputStream(originalImagePath, StandardOpenOption.READ);
        return new StreamedFile(inputStream, originalImageId.getType().getMediaType());
    }

    public ImageId uploadImage(@NonNull CompletedFileUpload fileUpload) throws IOException, NoSuchAlgorithmException {
        requireNonNull(fileUpload, "fileUpload");

        Path tempFile = Files.createTempFile(fileUpload.getFilename(), null);
        Files.copy(fileUpload.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        Optional<MimeType> mimeType = TikaUtils.detectMimeType(tempFile);

        if (mimeType.isEmpty()) {
            throw new UnsupportedFormatException();
        }

        Optional<ImageType> imageType = ImageType.findByExtension(mimeType.get().getExtension());

        if (imageType.isEmpty()) {
            throw new UnsupportedFormatException();
        }

        String hex;

        try (InputStream inputStream = Files.newInputStream(tempFile, StandardOpenOption.READ)) {
            hex = DigestUtils.sha256Hex(inputStream);
        }

        ImageId imageId = new ImageId(imageType.get(), hex);

        String filePath = folderPath + "/" + imageId.toString();

        Path path = Paths.get(filePath);

        if (Files.notExists(path)) {
            LOG.debug("Saving {}", path);

            Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
        }

        LOG.debug("Generated ID {}", imageId.toString());

        return imageId;
    }

}
