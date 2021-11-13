package com.gomezrondon.uplaodfile.controller;


import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestController
public class UploadFileController {

    private String directory;
    @Value("${gcs-resource-test-bucket}")
    private String bucketName;
    @Value("${gcs-file-object-name}")
    private String objectName;
    @Value("${gcs-flask-app-url}")
    private String flaskAppUrl;

    private final Storage storage;
    private final WebClient.Builder builder;

    public UploadFileController(Storage storage,  WebClient.Builder builder) {
        this.storage = storage;
        this.builder = builder;
        String currentDir = System.getProperty("user.dir");
        String separator = System.getProperty("file.separator");
        directory = currentDir + separator + "data" + separator;

        Path path = Paths.get(directory);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping("/time")
    public String getTime() throws IOException {
        return "the time is: "+ LocalDateTime.now();
    }

    @PostMapping("/stt/lang/{language}")
    public @ResponseBody
    byte[] uploadFile(@RequestParam("file") MultipartFile file, @PathVariable String language) throws IOException {
        String filePath = directory + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        this.upload(filePath);
        byte[] response = getAudioFile2(language);
        System.out.println("Post request executed! ");
        return response;
    }

    private byte[] getAudioFile2(String language) {
        var postBody = new PostBody(bucketName, language);
        Flux<DataBuffer> dataBufferFlux = builder.build()
                .post()
                .uri(flaskAppUrl + "/process")
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .body(Mono.just(postBody), PostBody.class)
                .retrieve()
                .bodyToFlux(DataBuffer.class);
        String currentDir = System.getProperty("user.dir");
        DataBufferUtils.write(dataBufferFlux, Path.of(currentDir+"/audio33.mp3"), StandardOpenOption.CREATE).block();

        return null;
    }

    public void upload(String filePath) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }


}
