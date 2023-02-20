package com.fileuploadapi.controller;

import com.fileuploadapi.exception.FileException;
import com.fileuploadapi.model.ResponseModel;
import com.fileuploadapi.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<ResponseModel> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        if (file.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseModel("Please select a file to upload"));
        try {
            String fileName = file.getOriginalFilename();
            fileService.uploadFile(file);

            /*String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/download/")
                    .path(fileName)
                    .toUriString();*/
            ResponseModel responseModel = new ResponseModel();
            responseModel.setFileName(fileName);
            responseModel.setSize(file.getSize());
            responseModel.setDownloadUri("/download/"+fileName); //fileDownloadUri  - for complete url
            responseModel.setMessage("SUCCESS");
            return ResponseEntity.status(HttpStatus.OK).body(responseModel);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseModel(message));
        }
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename,  HttpServletRequest request) throws IOException {
        // Load file as Resource
        Resource file = fileService.downloadFile(filename);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(file.getFile().getAbsolutePath());

            // Fallback to the default content type if type could not be determined
            if(contentType == null) {
                contentType = "application/octet-stream";
            }
            log.info("file: {}, file name: {}", file, file.getFilename());
        } catch (Exception ex) {
            log.info("Could not determine file type.");
            throw new FileException("Could not determine file type: " + ex);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

}
