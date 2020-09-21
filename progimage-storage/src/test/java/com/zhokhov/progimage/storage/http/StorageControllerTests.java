package com.zhokhov.progimage.storage.http;

import com.zhokhov.progimage.client.ProgImageClient;
import com.zhokhov.progimage.shared.UploadResponse;
import com.zhokhov.progimage.util.TikaUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.apache.tika.mime.MimeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.zhokhov.progimage.util.TestFileUtils.saveBytesToTempFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StorageControllerTests {

    @Inject EmbeddedServer embeddedServer;
    ProgImageClient progImageClient;

    @BeforeAll
    public void setup() {
        progImageClient = new ProgImageClient(
                embeddedServer.getURL(),
                embeddedServer.getURL(),
                embeddedServer.getURL(),
                embeddedServer.getURL(),
                embeddedServer.getURL()
        );
    }

    @Test
    public void uploadAndConversion() throws Exception {
        // given
        String file = StorageControllerTests.class.getResource("/testphoto1.jpeg").getFile();

        // when
        HttpResponse<UploadResponse> httpResponse = progImageClient.uploadImageBlocking(new File(file));

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());
        assertEquals(
                "1-bf4c8ac653044adafd4853a3c0945e69ddcfdce91c6728836ef86d5c17615633.jpg",
                httpResponse.getBody().get().getImageId()
        );

        // when
        HttpResponse<byte[]> imageResponse = progImageClient
                .retrieveImageBlocking("1-bf4c8ac653044adafd4853a3c0945e69ddcfdce91c6728836ef86d5c17615633.jpg");

        // then
        checkImageResponse(imageResponse, "image/jpeg");

        // when
        imageResponse = progImageClient
                .retrieveImageBlocking("1-bf4c8ac653044adafd4853a3c0945e69ddcfdce91c6728836ef86d5c17615633.gif");

        // then
        checkImageResponse(imageResponse, "image/gif");

        // when
        imageResponse = progImageClient
                .retrieveImageBlocking("1-bf4c8ac653044adafd4853a3c0945e69ddcfdce91c6728836ef86d5c17615633.bmp");

        // then
        checkImageResponse(imageResponse, "image/bmp");

        // when
        imageResponse = progImageClient
                .retrieveImageBlocking("1-bf4c8ac653044adafd4853a3c0945e69ddcfdce91c6728836ef86d5c17615633.png");

        // then
        checkImageResponse(imageResponse, "image/png");
    }

    @Test
    public void imageHash() {
        // given
        String file = StorageControllerTests.class.getResource("/testphoto2.jpg").getFile();

        // when
        HttpResponse<UploadResponse> httpResponse = progImageClient.uploadImageBlocking(new File(file));

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());
        assertEquals(
                "1-91457d7378d950bc620d2e428f9e5f395ebfd7ab88b032780ff422297ffec909.jpg",
                httpResponse.getBody().get().getImageId()
        );
    }

    @Test
    public void bulkUpload() {
        // given
        String file1 = StorageControllerTests.class.getResource("/testphoto1.jpeg").getFile();
        String file2 = StorageControllerTests.class.getResource("/testphoto2.jpg").getFile();

        // when
        Flowable<HttpResponse<UploadResponse>> responseFlowable = progImageClient.bulkUploadImages(
                List.of(new File(file1), new File(file2))
        );

        List<HttpResponse<UploadResponse>> result = StreamSupport
                .stream(responseFlowable.blockingIterable().spliterator(), false)
                .collect(Collectors.toList());

        // then
        assertEquals(2, result.size());
    }

    @Test
    public void ignoreNonImages() {
        // given
        String file = StorageControllerTests.class.getResource("/testtextfile.txt").getFile();

        // when
        var exception = assertThrows(HttpClientResponseException.class, () ->
                progImageClient.uploadImageBlocking(new File(file)));

        // then
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
    }

    private void checkImageResponse(HttpResponse<byte[]> imageResponse, String expectedContentType) throws IOException {
        assertNotNull(imageResponse);
        assertEquals(HttpStatus.OK, imageResponse.getStatus());
        assertTrue(imageResponse.getHeaders().getContentType().isPresent());
        assertEquals(expectedContentType, imageResponse.getHeaders().getContentType().get());
        assertTrue(imageResponse.getBody().isPresent());

        saveBytesToTempFile(imageResponse.body());

        Optional<MimeType> mimeType = TikaUtils.detectMimeType(imageResponse.getBody().get());

        assertTrue(mimeType.isPresent());
        assertEquals(expectedContentType, mimeType.get().getName());
    }

}
