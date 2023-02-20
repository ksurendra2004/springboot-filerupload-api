package com.fileuploadapi.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ResponseModel {

    private String fileName;
    private String downloadUri;
    private long size;
    private String message;


    public ResponseModel(String message) {
        this.message = message;
    }
}
