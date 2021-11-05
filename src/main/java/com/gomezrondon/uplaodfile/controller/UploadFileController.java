package com.gomezrondon.uplaodfile.controller;


import com.google.cloud.spring.vision.CloudVisionTemplate;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@RestController
@CrossOrigin(origins = {"${settings.cors_origin}"})
public class UploadFileController {

    private String directory;
    @Value("${gcs-resource-test-bucket}")
    private String bucketName;
    @Value("${gcs-file-object-name}")
    private String objectName;
    @Value("${gcs-flask-app-url}")
    private String flaskAppUrl;

    private final Storage storage;
    private final RestTemplate restTemplate;

    @Autowired private ResourceLoader resourceLoader;

    @Autowired
    private CloudVisionTemplate cloudVisionTemplate;

    public UploadFileController(Storage storage, RestTemplate restTemplate) {
        this.storage = storage;
        this.restTemplate = restTemplate;
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
    public String getTime() {
        String textFromImage = this.cloudVisionTemplate.extractTextFromImage(this.resourceLoader.getResource("classpath:eng-test.png"));
        return textFromImage;
    }

    /**
     * Convert a test file to an audio fiel (text to speech)
     * @param file
     * @param language
     * @return
     * @throws IOException
     */
    @PostMapping("/stt/lang/{language}")
    public @ResponseBody byte[] uploadFile(@RequestParam("file") MultipartFile file, @PathVariable String language) throws IOException {
        String filePath = directory + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        this.upload(filePath);
        ResponseEntity<byte[]> responseEntity = getAudioFile(language);
        System.out.println("Post request executed! ");
        return responseEntity.getBody();
    }

    /**
     * Call a python microservice to convert the text to an audio file
     * @param language chosen language
     * @return audio file in array of bytes
     */
    private ResponseEntity<byte[]> getAudioFile(String language) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var personJsonObject = new JSONObject();
        personJsonObject.put("bucket", bucketName);
        personJsonObject.put("lang", language);
        HttpEntity<String> request = new HttpEntity<String>(personJsonObject.toString(), headers);
        ResponseEntity<byte[]> responseEntity = restTemplate.postForEntity(flaskAppUrl+"/process", request, byte[].class);
        return responseEntity;
    }

    /**
     * Upload file to a gcp bucket
     * @param filePath path of the file
     * @throws IOException
     */
    public void upload(String filePath) throws IOException {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

        System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName);
    }


}
