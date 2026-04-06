package com.vibemyself.common.storage;

import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupabaseStorageService {

    private final WebClient supabaseWebClient;

    @Value("${supabase.url}")
    private final String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private final String serviceRoleKey;

    @Value("${supabase.storage.goods-bucket}")
    private final String goodsBucket;

    public String upload(MultipartFile file, String goodsNo) {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String path = goodsNo + "/" + filename;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        supabaseWebClient.put()
                .uri("/storage/v1/object/" + goodsBucket + "/" + path)
                .header("Authorization", "Bearer " + serviceRoleKey)
                .header("x-upsert", "true")
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : "application/octet-stream"
                ))
                .bodyValue(bytes)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> reactor.core.publisher.Mono.error(new AppException(ErrorCode.IMAGE_UPLOAD_FAILED)))
                .bodyToMono(String.class)
                .block();

        return supabaseUrl + "/storage/v1/object/public/" + goodsBucket + "/" + path;
    }
}
