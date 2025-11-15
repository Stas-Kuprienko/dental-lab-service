package org.lab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class WorkPhotoEntry implements Serializable {

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("photo_link")
    private String photoLink;


    public WorkPhotoEntry() {}
}
