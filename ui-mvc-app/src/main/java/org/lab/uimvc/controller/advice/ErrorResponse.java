package org.lab.uimvc.controller.advice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private int code;

    private String message;


    public ErrorResponse() {}
}
