package com.vibemyself.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "supabase")
public record SupabaseProperties(
        String url,
        String serviceRoleKey,
        Storage storage
) {
    public record Storage(String goodsBucket) {}
}
