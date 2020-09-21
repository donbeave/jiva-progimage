package com.zhokhov.progimage.compression;

import com.zhokhov.progimage.client.ProgImageClient;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
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
public class CompressionControllerTests {

    @Inject EmbeddedServer embeddedServer;
    ProgImageClient progImageClient;

    @BeforeAll
    public void setup() {
        progImageClient = new ProgImageClient(
                embeddedServer.getURL(), embeddedServer.getURL(), embeddedServer.getURL(), embeddedServer.getURL(),
                embeddedServer.getURL()
        );
    }

    @Test
    public void compressBmp() {
        // given
        String file = CompressionControllerTests.class.getResource("/testphoto.bmp").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.compressImageBlocking(new File(file), 0.1f);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(3499254, httpResponse.getContentLength());
    }

    @Test
    public void compressGif() {
        // given
        String file = CompressionControllerTests.class.getResource("/testphoto.gif").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.compressImageBlocking(new File(file), 0.1f);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(283008, httpResponse.getContentLength());
    }

    @Test
    public void compressJpg() {
        // given
        String file = CompressionControllerTests.class.getResource("/testphoto.jpg").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.compressImageBlocking(new File(file), 0.1f);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(30844, httpResponse.getContentLength());
    }

    @Test
    public void compressPng() {
        // given
        String file = CompressionControllerTests.class.getResource("/testphoto.png").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.compressImageBlocking(new File(file), 0.1f);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(1469902, httpResponse.getContentLength());
    }

}
