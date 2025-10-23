package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkPhotoFileData {

    @JsonProperty("data")
    private byte[] data;

    @JsonProperty("filename")
    private String filename;


    public WorkPhotoFileData() {}
}
