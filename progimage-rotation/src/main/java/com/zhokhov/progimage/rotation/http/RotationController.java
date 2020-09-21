package com.zhokhov.progimage.rotation.http;

import com.zhokhov.progimage.shared.ImageType;
import com.zhokhov.progimage.shared.RotationDegree;
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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

@Controller("/rotation")
public class RotationController {

    private final Logger LOG = LoggerFactory.getLogger(RotationController.class);

    @Post("/rotate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ExecuteOn(TaskExecutors.IO)
    public StreamedFile rotate(@NonNull CompletedFileUpload file, @QueryValue RotationDegree degree) throws IOException {
        LOG.debug("Rotating image: {}", file.getFilename());

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

        double rads = Math.toRadians(degree.getDegree());
        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));
        int width = (int) Math.floor(originalImage.getWidth() * cos + originalImage.getHeight() * sin);
        int height = (int) Math.floor(originalImage.getHeight() * cos + originalImage.getWidth() * sin);

        BufferedImage rotatedImage = new BufferedImage(width, height, originalImage.getType());

        AffineTransform at = new AffineTransform();
        at.translate(width / 2, height / 2);
        at.rotate(rads, 0, 0);
        at.translate(-originalImage.getWidth() / 2, -originalImage.getHeight() / 2);

        AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotateOp.filter(originalImage, rotatedImage);

        Path rotatedImagePath = Files.createTempFile("rotate", null);

        try (OutputStream os = Files.newOutputStream(rotatedImagePath)) {
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                ImageIO.write(rotatedImage, imageType.get().getFormatName(), ios);
            }
        }

        InputStream inputStream = Files.newInputStream(rotatedImagePath, StandardOpenOption.READ);
        return new StreamedFile(inputStream, imageType.get().getMediaType());
    }

}
