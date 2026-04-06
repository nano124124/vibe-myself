package com.vibemyself.common.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SupabaseStorageServiceTest {

    @Mock RestTemplate restTemplate;
    SupabaseStorageService service;

    @BeforeEach
    void setUp() {
        service = new SupabaseStorageService(
            restTemplate,
            "https://test.supabase.co",
            "test-service-key",
            "goods-images"
        );
    }

    @Test
    void upload_성공시_publicUrl_반환() {
        MockMultipartFile file = new MockMultipartFile(
            "images", "photo.jpg", "image/jpeg", "data".getBytes()
        );
        given(restTemplate.exchange(any(), eq(HttpMethod.PUT), any(), eq(String.class)))
            .willReturn(ResponseEntity.ok("{\"Key\":\"goods-images/G001/uuid_photo.jpg\"}"));

        String url = service.upload(file, "G001");

        assertThat(url).startsWith("https://test.supabase.co/storage/v1/object/public/goods-images/G001/");
        assertThat(url).endsWith("_photo.jpg");
    }

    @Test
    void upload_실패시_예외발생() {
        MockMultipartFile file = new MockMultipartFile(
            "images", "photo.jpg", "image/jpeg", "data".getBytes()
        );
        given(restTemplate.exchange(any(), eq(HttpMethod.PUT), any(), eq(String.class)))
            .willReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).body("error"));

        assertThatThrownBy(() -> service.upload(file, "G001"))
            .isInstanceOf(RuntimeException.class);
    }
}
