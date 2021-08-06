package com.gomezrondon.uplaodfile.controller;


import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
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

    private final Storage storage;

    public UploadFileController(Storage storage) {
        this.storage = storage;
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

    @GetMapping(value = "/image") //just testing
    public @ResponseBody byte[] getImage() throws IOException {
        String file = directory + "thunbnail.png";
        byte[] bytes = Files.readAllBytes(Paths.get(file));
        return bytes;
    }

    public void upload(String filePath) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        System.out.println(
                "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }


}
