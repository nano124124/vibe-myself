package com.vibemyself.common.storage;

import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final WebClient supabaseWebClient;
    private final String supabaseUrl;
    private final String serviceRoleKey;
    private final String goodsBucket;

    public SupabaseStorageService(
            WebClient supabaseWebClient,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey,
            @Value("${supabase.storage.goods-bucket}") String goodsBucket) {
        this.supabaseWebClient = supabaseWebClient;
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
        this.goodsBucket = goodsBucket;
    }

    public String upload(MultipartFile file, String goodsNo) {
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image";
        String filename = UUID.randomUUID() + "_" + originalFilename;
        String path = goodsNo + "/" + filename;

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        supabaseWebClient.put()
                .uri(b -> b.path("/storage/v1/object/{bucket}/{path}")
                        .build(goodsBucket, path))
                .header("Authorization", "Bearer " + serviceRoleKey)
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

        return supabaseUrl + "/storage/v1/object/public/" + goodsBucket + "/" + path;
    }
}
