package com.gomezrondon.uplaodfile.controller;


import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public Mono<String> getTime() {
        return Mono.just("the time is: "+ LocalDateTime.now());
    }


    @PostMapping(value = "/upload/file2")
    public Mono<Void> uploadHandler(@RequestBody Flux<Part> parts) {

        Path path = Paths.get(directory);

        return uploadFileReturnFileName(parts, path)
                .flatMap(filename -> {
                    try {
                        upload(path.resolve(filename).toAbsolutePath().toString(), bucketName, filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Mono.empty();
                })
                .then();
    }

    @PostMapping("/stt/lang/{language}")
    public @ResponseBody Flux<byte[]> uploadFile(@RequestBody Flux<Part> parts, @PathVariable String language) throws IOException {

        Path path = Paths.get(directory);
        System.out.println("Post request executed! ");

        return uploadFileReturnFileName(parts, path)
                .flatMap(filename -> {
                    try {
                        upload(path.resolve(filename).toAbsolutePath().toString(), bucketName, objectName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return Mono.empty();
                }) // upload to gcp bucket
                .thenMany(getAudioFile2(language));  // give info to flask app where is file in bucket to convert to byte[]
    }

    private Flux<String> uploadFileReturnFileName(@RequestBody Flux<Part> parts, Path path) {
        return parts
                .filter(part -> part instanceof FilePart) // only retain file parts
                .ofType(FilePart.class) // convert the flux to FilePart
                .flatMap(filePart -> {
                    return filePart.transferTo(path.resolve(filePart.filename())) //returns Mono<void>
                            .then(Mono.just(filePart.filename())); // then I return the filename
                });
    }

    private Mono<byte[]> getAudioFile2(String language) {
        var postBody = new PostBody(bucketName, language);
        return builder.build()
                .post()
                .uri(flaskAppUrl + "/process")
                .contentType(APPLICATION_JSON)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .body(Mono.just(postBody), PostBody.class)
                .retrieve()
                .bodyToMono(byte[].class);

    }

    public void upload(String filePath, String bucketName, String objectName) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }


/*    @PostMapping("/stt/lang/{language}")
    public @ResponseBody byte[] uploadFile(@RequestParam("file") MultipartFile file, @PathVariable String language) throws IOException {
        String filePath = directory + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        this.upload(filePath);
        byte[] response = getAudioFile2(language);
        System.out.println("Post request executed! ");
        return response;
    }*/


}
