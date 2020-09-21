package com.zhokhov.progimage.filters.http;

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
public class FiltersControllerBlackAndWhiteTests {

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
    public void blackAndWhiteBmp() {
        // given
        String file = FiltersControllerBlackAndWhiteTests.class.getResource("/testphoto.bmp").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.applyBlackAndWhiteFilterBlocking(new File(file));

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(146942, httpResponse.getContentLength());
    }

    @Test
    public void blackAndWhiteGif() {
        // given
        String file = FiltersControllerBlackAndWhiteTests.class.getResource("/testphoto.gif").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.applyBlackAndWhiteFilterBlocking(new File(file));

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(14999, httpResponse.getContentLength());
    }

    @Test
    public void blackAndWhiteJpg() {
        // given
        String file = FiltersControllerBlackAndWhiteTests.class.getResource("/testphoto.jpg").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.applyBlackAndWhiteFilterBlocking(new File(file));

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(90529, httpResponse.getContentLength());
    }

    @Test
    public void blackAndWhitePng() {
        // given
        String file = FiltersControllerBlackAndWhiteTests.class.getResource("/testphoto.png").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.applyBlackAndWhiteFilterBlocking(new File(file));

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(12555, httpResponse.getContentLength());
    }

}
