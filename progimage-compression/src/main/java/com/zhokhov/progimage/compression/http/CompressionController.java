package com.zhokhov.progimage.compression.http;

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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Optional;

@Controller("/compression")
public class CompressionController {

    private final Logger LOG = LoggerFactory.getLogger(CompressionController.class);

    @Post("/compress")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ExecuteOn(TaskExecutors.IO)
    public StreamedFile compress(@NonNull CompletedFileUpload file, @QueryValue float quality) throws IOException {
        LOG.debug("Compressing image: {}", file.getFilename());

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

        Path compressedImagePath = Files.createTempFile("compression", imageType.get().getExtension());

        try (OutputStream os = Files.newOutputStream(compressedImagePath)) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(imageType.get().getFormatName());
            ImageWriter writer = writers.next();

            try {
                try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
                    ImageWriteParam param = writer.getDefaultWriteParam();
                    if (param.canWriteCompressed()) {
                        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

                        String[] compressionTypes = param.getCompressionTypes();

                        if (compressionTypes.length >= 1) {
                            param.setCompressionType(compressionTypes[0]);
                        }
                        param.setCompressionQuality(quality);
                    }

                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(originalImage, null, null), param);
                }
            } finally {
                writer.dispose();
            }
        }

        InputStream inputStream = Files.newInputStream(compressedImagePath, StandardOpenOption.READ);
        return new StreamedFile(inputStream, imageType.get().getMediaType());
    }

}
