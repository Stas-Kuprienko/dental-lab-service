package org.lab.model;

import java.io.Serializable;

public record ErrorResponse(String status, String message) implements Serializable {}
