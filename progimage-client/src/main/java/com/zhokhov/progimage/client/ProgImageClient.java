package com.zhokhov.progimage.client;

import com.zhokhov.progimage.shared.RotationDegree;
import com.zhokhov.progimage.shared.UploadResponse;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.http.uri.UriBuilder;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Publisher;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ProgImageClient {

    private final URL storageBaseUrl;
    private final URL compressionBaseUrl;
    private final URL rotationBaseUrl;
    private final URL filtersBaseUrl;
    private final URL thumbnailBaseUrl;
    private final HttpClient httpClient;

    public ProgImageClient() throws MalformedURLException {
        this(
                new URL("http://localhost:10001/"),
                new URL("http://localhost:10002/"),
                new URL("http://localhost:10003/"),
                new URL("http://localhost:10004/"),
                new URL("http://localhost:10005/")
        );
    }

    public ProgImageClient(@NonNull URL storageBaseUrl,
                           @NonNull URL compressionBaseUrl,
                           @NonNull URL rotationBaseUrl,
                           @NonNull URL filtersBaseUrl,
                           @NonNull URL thumbnailBaseUrl) {
        this(
                storageBaseUrl, compressionBaseUrl, rotationBaseUrl, filtersBaseUrl, thumbnailBaseUrl,
                HttpClient.create(null)
        );
    }

    /**
     * Initialize a ProgImage client.
     *
     * @param storageBaseUrl     base url to ProgImage storage microservice
     * @param compressionBaseUrl base url to ProgImage compression microservice
     * @param rotationBaseUrl    base url to ProgImage rotation microservice
     * @param filtersBaseUrl     base url to ProgImage filters microservice
     * @param thumbnailBaseUrl   base url to ProgImage rotation microservice
     * @param httpClient         http client used to perform all requests
     */
    public ProgImageClient(@NonNull URL storageBaseUrl,
                           @NonNull URL compressionBaseUrl,
                           @NonNull URL rotationBaseUrl,
                           @NonNull URL filtersBaseUrl,
                           @NonNull URL thumbnailBaseUrl,
                           @NonNull HttpClient httpClient) {
        this.storageBaseUrl = Objects.requireNonNull(storageBaseUrl, "storageBaseUrl");
        this.compressionBaseUrl = Objects.requireNonNull(compressionBaseUrl, "compressionBaseUrl");
        this.rotationBaseUrl = Objects.requireNonNull(rotationBaseUrl, "rotationBaseUrl");
        this.filtersBaseUrl = Objects.requireNonNull(filtersBaseUrl, "filtersBaseUrl");
        this.thumbnailBaseUrl = Objects.requireNonNull(thumbnailBaseUrl, "thumbnailBaseUrl");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public Publisher<HttpResponse<UploadResponse>> uploadImage(@NonNull File file) {
        requireNonNull(file, "file");

        URI uri = UriBuilder.of("/storage/upload")
                .scheme(storageBaseUrl.getProtocol())
                .host(storageBaseUrl.getHost())
                .port(storageBaseUrl.getPort())
                .build();

        MultipartBody multipartBody = MultipartBody.builder()
                .addPart("file", file)
                .build();

        HttpRequest<MultipartBody> req = HttpRequest.POST(uri, multipartBody)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return httpClient.exchange(req, UploadResponse.class);
    }

    public Flowable<HttpResponse<UploadResponse>> bulkUploadImages(@NonNull List<File> files) {
        return Flowable.fromIterable(files).flatMap(this::uploadImage);
    }

    public HttpResponse<UploadResponse> uploadImageBlocking(@NonNull File file) {
        return Flowable.fromPublisher(uploadImage(file)).blockingFirst();
    }

    /**
     * Retrieve image from repository.
     *
     * @param imageId unique image id retrieved after upload image to the repository
     */
    public Publisher<HttpResponse<byte[]>> retrieveImage(@NonNull String imageId) {
        requireNonNull(imageId, "imageId");

        URI uri = UriBuilder.of("/storage/retrieve/")
                .path(imageId)
                .scheme(storageBaseUrl.getProtocol())
                .host(storageBaseUrl.getHost())
                .port(storageBaseUrl.getPort())
                .build();
        HttpRequest<byte[]> req = HttpRequest.GET(uri);

        return httpClient.exchange(req, byte[].class);
    }

    public HttpResponse<byte[]> retrieveImageBlocking(String imageId) {
        return Flowable.fromPublisher(retrieveImage(imageId)).blockingFirst();
    }

    /**
     * Perform compressing operation.
     *
     * @param file    image file to compress
     * @param quality desired compression quality (min is 0.01, max is 1.00)
     * @return compressed image
     */
    public Publisher<HttpResponse<byte[]>> compressImage(@NonNull File file, float quality) {
        requireNonNull(file, "file");

        URI uri = UriBuilder.of("/compression/compress")
                .queryParam("quality", quality)
                .scheme(compressionBaseUrl.getProtocol())
                .host(compressionBaseUrl.getHost())
                .port(compressionBaseUrl.getPort())
                .build();

        MultipartBody multipartBody = MultipartBody.builder()
                .addPart("file", file)
                .build();

        HttpRequest<MultipartBody> req = HttpRequest.POST(uri, multipartBody)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return httpClient.exchange(req, byte[].class);
    }

    public HttpResponse<byte[]> compressImageBlocking(@NonNull File file, float quality) {
        return Flowable.fromPublisher(compressImage(file, quality)).blockingFirst();
    }

    public Flowable<HttpResponse<byte[]>> bulkCompressImages(@NonNull List<File> files, float quality) {
        return Flowable.fromIterable(files).flatMap(file -> compressImage(file, quality));
    }

    public Flowable<HttpResponse<byte[]>> bulkCompressImagesFromRepository(@NonNull List<String> imageIds, float quality) {
        return Flowable.fromIterable(imageIds)
                .observeOn(Schedulers.io())
                .flatMap(imageId -> retrieveImage(imageId))
                .map(response -> writeByteArrayToFile(response.body()))
                .flatMap(file -> compressImage(file, quality));
    }

    public Flowable<HttpResponse<byte[]>> bulkCompressRemoteImages(@NonNull List<URL> urls, float quality) {
        return Flowable.fromIterable(urls)
                .observeOn(Schedulers.io())
                .map(url -> Paths.get(url.toURI()).toFile())
                .flatMap(file -> compressImage(file, quality));
    }

    public Publisher<HttpResponse<byte[]>> rotateImage(@NonNull File file, @NonNull RotationDegree degree) {
        requireNonNull(file, "file");
        requireNonNull(degree, "degree");

        URI uri = UriBuilder.of("/rotation/rotate")
                .queryParam("degree", degree)
                .scheme(rotationBaseUrl.getProtocol())
                .host(rotationBaseUrl.getHost())
                .port(rotationBaseUrl.getPort())
                .build();

        MultipartBody multipartBody = MultipartBody.builder()
                .addPart("file", file)
                .build();

        HttpRequest<MultipartBody> req = HttpRequest.POST(uri, multipartBody)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return httpClient.exchange(req, byte[].class);
    }

    public HttpResponse<byte[]> rotateImageBlocking(@NonNull File file, @NonNull RotationDegree degree) {
        return Flowable.fromPublisher(rotateImage(file, degree)).blockingFirst();
    }

    public Flowable<HttpResponse<byte[]>> bulkRotateImages(@NonNull List<File> files, @NonNull RotationDegree degree) {
        return Flowable.fromIterable(files).flatMap(file -> rotateImage(file, degree));
    }

    public Flowable<HttpResponse<byte[]>> bulkRotateImagesFromRepository(@NonNull List<String> imageIds,
                                                                         @NonNull RotationDegree degree) {
        return Flowable.fromIterable(imageIds)
                .observeOn(Schedulers.io())
                .flatMap(imageId -> retrieveImage(imageId))
                .map(response -> writeByteArrayToFile(response.body()))
                .flatMap(file -> rotateImage(file, degree));
    }

    public Flowable<HttpResponse<byte[]>> bulkRotateRemoteImages(@NonNull List<URL> urls,
                                                                 @NonNull RotationDegree degree) {
        return Flowable.fromIterable(urls)
                .observeOn(Schedulers.io())
                .map(url -> Paths.get(url.toURI()).toFile())
                .flatMap(file -> rotateImage(file, degree));
    }

    public Publisher<HttpResponse<byte[]>> applyBlackAndWhiteFilter(@NonNull File file) {
        requireNonNull(file, "file");

        URI uri = UriBuilder.of("/filters/black-and-white")
                .scheme(filtersBaseUrl.getProtocol())
                .host(filtersBaseUrl.getHost())
                .port(filtersBaseUrl.getPort())
                .build();

        MultipartBody multipartBody = MultipartBody.builder()
                .addPart("file", file)
                .build();

        HttpRequest<MultipartBody> req = HttpRequest.POST(uri, multipartBody)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return httpClient.exchange(req, byte[].class);
    }

    public HttpResponse<byte[]> applyBlackAndWhiteFilterBlocking(@NonNull File file) {
        return Flowable.fromPublisher(applyBlackAndWhiteFilter(file)).blockingFirst();
    }

    public Flowable<HttpResponse<byte[]>> bulkApplyBlackAndWhiteFilter(@NonNull List<File> files) {
        return Flowable.fromIterable(files).flatMap(this::applyBlackAndWhiteFilter);
    }

    public Flowable<HttpResponse<byte[]>> bulkApplyBlackAndWhiteFilterFromRepository(@NonNull List<String> imageIds) {
        return Flowable.fromIterable(imageIds)
                .observeOn(Schedulers.io())
                .flatMap(this::retrieveImage)
                .map(response -> writeByteArrayToFile(response.body()))
                .flatMap(this::applyBlackAndWhiteFilter);
    }

    public Flowable<HttpResponse<byte[]>> bulkApplyBlackAndWhiteFilterRemoteImages(@NonNull List<URL> urls) {
        return Flowable.fromIterable(urls)
                .observeOn(Schedulers.io())
                .map(url -> Paths.get(url.toURI()).toFile())
                .flatMap(this::applyBlackAndWhiteFilter);
    }

    public Publisher<HttpResponse<byte[]>> applyGrayscaleFilter(@NonNull File file) {
        requireNonNull(file, "file");

        URI uri = UriBuilder.of("/filters/grayscale")
                .scheme(filtersBaseUrl.getProtocol())
                .host(filtersBaseUrl.getHost())
                .port(filtersBaseUrl.getPort())
                .build();

        MultipartBody multipartBody = MultipartBody.builder()
                .addPart("file", file)
                .build();

        HttpRequest<MultipartBody> req = HttpRequest.POST(uri, multipartBody)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return httpClient.exchange(req, byte[].class);
    }

    public HttpResponse<byte[]> applyGrayscaleFilterBlocking(@NonNull File file) {
        return Flowable.fromPublisher(applyGrayscaleFilter(file)).blockingFirst();
    }

    public Flowable<HttpResponse<byte[]>> bulkApplyGrayscaleFilter(@NonNull List<File> files) {
        return Flowable.fromIterable(files).flatMap(this::applyGrayscaleFilter);
    }

    public Flowable<HttpResponse<byte[]>> bulkApplyGrayscaleFilterFromRepository(@NonNull List<String> imageIds) {
        return Flowable.fromIterable(imageIds)
                .observeOn(Schedulers.io())
                .flatMap(this::retrieveImage)
                .map(response -> writeByteArrayToFile(response.body()))
                .flatMap(this::applyGrayscaleFilter);
    }

    public Flowable<HttpResponse<byte[]>> bulkApplyGrayscaleFilterRemoteImages(@NonNull List<URL> urls) {
        return Flowable.fromIterable(urls)
                .observeOn(Schedulers.io())
                .map(url -> Paths.get(url.toURI()).toFile())
                .flatMap(this::applyGrayscaleFilter);
    }

    public Publisher<HttpResponse<byte[]>> createThumbnail(@NonNull File file, int width, int height) {
        requireNonNull(file, "file");

        URI uri = UriBuilder.of("/thumbnail/scale")
                .queryParam("width", width)
                .queryParam("height", height)
                .scheme(thumbnailBaseUrl.getProtocol())
                .host(thumbnailBaseUrl.getHost())
                .port(thumbnailBaseUrl.getPort())
                .build();

        MultipartBody multipartBody = MultipartBody.builder()
                .addPart("file", file)
                .build();

        HttpRequest<MultipartBody> req = HttpRequest.POST(uri, multipartBody)
                .contentType(MediaType.MULTIPART_FORM_DATA);

        return httpClient.exchange(req, byte[].class);
    }

    public HttpResponse<byte[]> createThumbnailBlocking(@NonNull File file, int width, int height) {
        return Flowable.fromPublisher(createThumbnail(file, width, height)).blockingFirst();
    }

    public Flowable<HttpResponse<byte[]>> bulkCreateThumbnails(@NonNull List<File> files, int width, int height) {
        return Flowable.fromIterable(files).flatMap(file -> createThumbnail(file, width, height));
    }

    public Flowable<HttpResponse<byte[]>> bulkCreateThumbnailsFromRepository(@NonNull List<String> imageIds,
                                                                             int width, int height) {
        return Flowable.fromIterable(imageIds)
                .observeOn(Schedulers.io())
                .flatMap(this::retrieveImage)
                .map(response -> writeByteArrayToFile(response.body()))
                .flatMap(file -> createThumbnail(file, width, height));
    }

    public Flowable<HttpResponse<byte[]>> bulkCreateThumbnailsRemoteImages(@NonNull List<URL> urls,
                                                                           int width, int height) {
        return Flowable.fromIterable(urls)
                .observeOn(Schedulers.io())
                .map(url -> Paths.get(url.toURI()).toFile())
                .flatMap(file -> createThumbnail(file, width, height));
    }

    private File writeByteArrayToFile(byte[] bytes) throws IOException {
        Path path = Files.createTempFile("client-cache", null);
        return Files.write(path, bytes).toFile();
    }

}
