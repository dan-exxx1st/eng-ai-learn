package com.devlingo.file;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class TxtTextExtractor implements TextExtractor {

    @Override
    public String extract(byte[] content) {
        return new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public boolean supports(String contentType) {
        return "text/plain".equals(contentType);
    }
}
