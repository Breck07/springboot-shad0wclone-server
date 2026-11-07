package com.example.demo;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service 
public class LoggerService {
    private Path logDir;
    public LoggerService(@Value("${app.log-dir}") String logDir){
        this.logDir = Path.of(logDir).toAbsolutePath().normalize();
    }

    public void logData(String data){
        // Turn string into bytes and log in file
        try{
            byte[] stringBytes = data.getBytes();
            Files.write(logDir, stringBytes);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
