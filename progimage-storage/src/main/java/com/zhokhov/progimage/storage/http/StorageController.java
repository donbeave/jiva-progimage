package com.zhokhov.progimage.storage.http;

import com.zhokhov.progimage.shared.ImageType;
import com.zhokhov.progimage.shared.UploadResponse;
import com.zhokhov.progimage.storage.repository.ImageId;
import com.zhokhov.progimage.storage.repository.ImageLocalStorageRepository;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.apache.tika.mime.MimeTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Controller("/storage")
public class StorageController {

    private static final Logger LOG = LoggerFactory.getLogger(StorageController.class);

    private final ImageLocalStorageRepository imageLocalStorageRepository;

    public StorageController(ImageLocalStorageRepository imageLocalStorageRepository) {
        this.imageLocalStorageRepository = imageLocalStorageRepository;
    }

    @Post("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<UploadResponse> upload(@NonNull CompletedFileUpload file) throws NoSuchAlgorithmException, IOException, MimeTypeException {
        LOG.debug("Uploading image: {}", file.getFilename());

        ImageId imageId = imageLocalStorageRepository.uploadImage(file);

        return HttpResponse.ok().body(new UploadResponse(imageId.toString()));
    }

    @Get("/retrieve/{imageId}")
    @ExecuteOn(TaskExecutors.IO)
    public StreamedFile retrieve(@NonNull String imageId) throws IOException {
        LOG.debug("Retrieving image: {}", imageId);

        String extension = imageId.contains(".") ? imageId.substring(imageId.indexOf(".")).toLowerCase() : null;

        if (StringUtils.isEmpty(extension)) {
            throw new FileNotFoundException(imageId);
        }

        if (extension.equals(".jpeg")) {
            extension = ".jpg";
        }

        ImageId originalImageId = ImageId.parseOriginal(imageId);
        Optional<ImageType> requestedImageType = ImageType.findByExtension(extension);

        if (requestedImageType.isEmpty()) {
            throw new FileNotFoundException();
        }

        return imageLocalStorageRepository.retrieveImage(originalImageId, requestedImageType.orElse(null));
    }

}
