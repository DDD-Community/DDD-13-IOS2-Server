package com.bangawo.storage.infrastructure.gcs;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcsConfig {

    private static final String PROJECT_ID = "aqueous-cargo-457101-h6";

    @Bean
    public Storage gcsStorage() {
        return StorageOptions.newBuilder()
                .setProjectId(PROJECT_ID)
                .build()
                .getService();
    }
}
