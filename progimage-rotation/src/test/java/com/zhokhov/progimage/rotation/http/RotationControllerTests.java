package com.zhokhov.progimage.rotation.http;

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

import static com.zhokhov.progimage.shared.RotationDegree.ROTATE_180;
import static com.zhokhov.progimage.shared.RotationDegree.ROTATE_270;
import static com.zhokhov.progimage.shared.RotationDegree.ROTATE_90;
import static com.zhokhov.progimage.util.TestFileUtils.saveBytesToTempFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RotationControllerTests {

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
    public void rotateBmp() {
        // given
        String file = RotationControllerTests.class.getResource("/testphoto.bmp").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.rotateImageBlocking(new File(file), ROTATE_90);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(3499254, httpResponse.getContentLength());
    }

    @Test
    public void rotateGif() {
        // given
        String file = RotationControllerTests.class.getResource("/testphoto.gif").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.rotateImageBlocking(new File(file), ROTATE_180);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(304290, httpResponse.getContentLength());
    }

    @Test
    public void rotateJpg() {
        // given
        String file = RotationControllerTests.class.getResource("/testphoto.jpg").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.rotateImageBlocking(new File(file), ROTATE_270);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(107589, httpResponse.getContentLength());
    }

    @Test
    public void rotatePng() {
        // given
        String file = RotationControllerTests.class.getResource("/testphoto.png").getFile();

        // when
        HttpResponse<byte[]> httpResponse = progImageClient.rotateImageBlocking(new File(file), ROTATE_270);

        // then
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.OK, httpResponse.getStatus());
        assertTrue(httpResponse.getBody().isPresent());

        saveBytesToTempFile(httpResponse.body());

        assertEquals(1652490, httpResponse.getContentLength());
    }

}
