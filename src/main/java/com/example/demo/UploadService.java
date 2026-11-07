package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;

/**
 * uploadService
 */
public class UploadService {
    private final Path uploadDirectory;

    public UploadService(@Value("${app.upload-dir}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public void saveFolder(InputStream input) throws IOException {
        Files.createDirectories(uploadDirectory);

        Path destination = uploadDirectory.resolve(UUID.randomUUID() + ".tar");

        // Read the incoming bytes into a buffer and write them to a folder
        try(InputStream in = input;
            OutputStream out = Files.newOutputStream(
                destination, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE)){
            byte[] buffer = new byte[8192];
            int count;
            
            while((count = in.read(buffer)) != -1){
                out.write(buffer, 0, count);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }  
}
