package com.vibemyself.common.storage;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SupabaseStorageServiceTest {

    MockWebServer server;
    SupabaseStorageService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        WebClient webClient = WebClient.builder()
                .baseUrl(server.url("/").toString())
                .build();
        service = new SupabaseStorageService(webClient, server.url("/").toString(), "test-key", "goods-images");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void upload_성공시_publicUrl_반환() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"Key\":\"goods-images/G001/uuid_photo.jpg\"}"));
        MockMultipartFile file = new MockMultipartFile(
                "images", "photo.jpg", "image/jpeg", "data".getBytes());

        String url = service.upload(file, "G001");

        assertThat(url).contains("/storage/v1/object/public/goods-images/G001/");
        assertThat(url).endsWith("_photo.jpg");
    }

    @Test
    void upload_실패시_예외발생() {
        server.enqueue(new MockResponse().setResponseCode(403).setBody("Forbidden"));
        MockMultipartFile file = new MockMultipartFile(
                "images", "photo.jpg", "image/jpeg", "data".getBytes());

        assertThatThrownBy(() -> service.upload(file, "G001"))
                .isInstanceOf(RuntimeException.class);
    }
}
