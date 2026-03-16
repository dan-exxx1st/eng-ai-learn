package com.devlingo.file;

public interface TextExtractor {
    String extract(byte[] content);
    boolean supports(String contentType);
}
