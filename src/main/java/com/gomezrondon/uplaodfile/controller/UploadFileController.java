package com.gomezrondon.uplaodfile.controller;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@RestController
public class UploadFileController {

    private String directory;
    @Value("${gcs-resource-test-bucket}")
    private String bucketName;
    @Value("${gcs-file-object-name}")
    private String objectName;
    @Value("${gcs-upload-flag:false}")
    private Boolean isUpload;


    public UploadFileController() {
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

    @PostMapping("/file-upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String filePath = directory + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        if (isUpload) {
            this.upload(filePath);
        }
        return "success";
    }

    public void upload(String filePath) throws IOException {
        Resource resource = new ClassPathResource("mycreds.json");
        InputStream inputStream = resource.getInputStream();

        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(inputStream);

        Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();

//        Storage storage = StorageOptions.getDefaultInstance().getService();

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        System.out.println(
                "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }


}
