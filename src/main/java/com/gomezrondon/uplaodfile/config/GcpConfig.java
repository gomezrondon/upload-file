package com.gomezrondon.uplaodfile.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GcpConfig {


    @Bean
    @Profile("gcp")
    public Storage getGCPStorage() throws IOException {
        return StorageOptions.getDefaultInstance().getService();
    }


    @Bean
    @Profile("!gcp")
    public Storage getStorage() throws IOException {
        Resource resource = new ClassPathResource("mycreds.json");
        InputStream inputStream = resource.getInputStream();

        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream);

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
//                .setConnectTimeout(Duration.ofMillis(60000))
//                .setReadTimeout(Duration.ofMillis(60000))
                .build();
    }


}
