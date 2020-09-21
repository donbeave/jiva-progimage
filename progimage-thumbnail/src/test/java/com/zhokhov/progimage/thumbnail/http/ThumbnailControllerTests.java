package com.zhokhov.progimage.thumbnail.http;

import com.zhokhov.progimage.client.ProgImageClient;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.inject.Inject;
import java.io.File;

import static com.zhokhov.progimage.util.TestFileUtils.saveBytesToTempFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ThumbnailControllerTests {

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
    public void thumbnailBmp() {
        // given
        String file = ThumbnailControllerTests.class.getResource("/testphoto.bmp").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.createThumbnailBlocking(new File(file), 50, 50);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(7654, httpResponse.getContentLength());
    }

    @Test
    public void thumbnailGif() throws Exception {
        // given
        String file = ThumbnailControllerTests.class.getResource("/testphoto.gif").getFile();

        MultipartBody multipartBody = MultipartBody.builder()
                .addPart("file", new File(file))
                .build();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.createThumbnailBlocking(new File(file), 100, 100);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(6606, httpResponse.getContentLength());
    }

    @Test
    public void thumbnailJpg() {
        // given
        String file = ThumbnailControllerTests.class.getResource("/testphoto.jpg").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.createThumbnailBlocking(new File(file), 150, 150);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(4570, httpResponse.getContentLength());
    }

    @Test
    public void thumbnailPng() {
        // given
        String file = ThumbnailControllerTests.class.getResource("/testphoto.png").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.createThumbnailBlocking(new File(file), 150, 100);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(37043, httpResponse.getContentLength());
    }

}
