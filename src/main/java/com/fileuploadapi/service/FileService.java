package com.fileuploadapi.service;

import com.fileuploadapi.exception.FileException;
import com.fileuploadapi.exception.FileNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileService {

    @Value("${output.file.path}")
    private String uploadDirPath;

    public void uploadFile(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            Path root = Paths.get(uploadDirPath);

            if(!Files.exists(root))
                Files.createDirectories(root);

            Path path = Paths.get(uploadDirPath + file.getOriginalFilename());
            Files.write(path, bytes);
        } catch (IOException e) {
            throw new FileException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    public Resource downloadFile(String filename) {
        try {
            Path downloadDirPath = Paths.get(uploadDirPath);
            log.info("downloadDirPath: {}", downloadDirPath);
            Path file = downloadDirPath.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            log.info("file: {}, resource: {}",file, resource);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + filename);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("File not found " + filename, e);
        }
    }
}
