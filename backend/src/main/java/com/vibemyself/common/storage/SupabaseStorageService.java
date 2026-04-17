package com.vibemyself.common.storage;

import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final WebClient supabaseWebClient;
    private final SupabaseProperties properties;

    public String upload(MultipartFile file, String goodsNo) {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
        String filename = UUID.randomUUID() + "_" + originalFilename;
        String path = String.join("/", goodsNo, filename);

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        supabaseWebClient.put()
                .uri(b -> b.path("/storage/v1/object/{bucket}/{path}")
                        .build(properties.storage().goodsBucket(), path))
                .header("Authorization", "Bearer " + properties.serviceRoleKey())
                .header("x-upsert", "true")
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream"
                ))
                .bodyValue(bytes)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> Mono.error(new AppException(ErrorCode.IMAGE_UPLOAD_FAILED)))
                .bodyToMono(String.class)
                .block();

        return properties.url() + "/storage/v1/object/public/" + properties.storage().goodsBucket() + "/" + path;
    }

    public void deleteAll(List<String> urls) {
        if (urls.isEmpty()) {
            return;
        }
        String urlPrefix = properties.url() + "/storage/v1/object/public/" + properties.storage().goodsBucket() + "/";
        List<String> paths = urls.stream()
                .map(url -> url.replace(urlPrefix, ""))
                .toList();

        try {
            supabaseWebClient.method(HttpMethod.DELETE)
                    .uri(b -> b.path("/storage/v1/object/{bucket}").build(properties.storage().goodsBucket()))
                    .header("Authorization", "Bearer " + properties.serviceRoleKey())
                    .bodyValue(Map.of("prefixes", paths))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("업로드된 이미지 삭제 실패. paths={}", paths, e);
        }
    }
}
