package com.devlingo.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class PdfTextExtractor implements TextExtractor {

    @Override
    public String extract(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to extract text from PDF", e);
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }

    @Override
    public boolean supports(String contentType) {
        return "application/pdf".equals(contentType);
    }
}
