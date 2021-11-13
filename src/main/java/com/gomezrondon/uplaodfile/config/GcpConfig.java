package com.gomezrondon.uplaodfile.config;


import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@Configuration
public class GcpConfig {


    @Bean
    public WebClient.Builder getWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Profile("gcp")
    public Storage getGCPStorage() throws IOException {
        return StorageOptions.getDefaultInstance().getService();
    }


}
