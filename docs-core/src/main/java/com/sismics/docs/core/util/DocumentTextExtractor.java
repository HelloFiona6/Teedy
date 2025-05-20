package com.sismics.docs.core.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sismics.util.mime.MimeType;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.docs.core.dao.UserDao;
import com.sismics.docs.core.model.jpa.User;
import com.google.common.io.ByteStreams;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class DocumentTextExtractor {
    private static final Logger log = LoggerFactory.getLogger(DocumentTextExtractor.class);

    public static String extractText(File file, String mimeType, String userId) throws IOException {
        // 检查文件是否存在
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        // 检查文件是否可读
        if (!file.canRead()) {
            throw new IOException("File is not readable: " + file.getAbsolutePath());
        }

        // 检查文件是否为空
        if (file.length() == 0) {
            throw new IOException("File is empty: " + file.getAbsolutePath());
        }

        log.info("Extracting text from file: {} with MIME type: {}", file.getName(), mimeType);

        try {
            // 创建临时文件用于解密
            java.nio.file.Path tempFile = AppContext.getInstance().getFileService().createTemporaryFile();
            
            // 获取文件创建者
            UserDao userDao = new UserDao();
            User user = userDao.getById(userId);
            if (user == null) {
                throw new IOException("File creator not found");
            }

            // 解密文件到临时文件
            try (InputStream fileInputStream = Files.newInputStream(file.toPath());
                 InputStream decryptedStream = EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey());
                 OutputStream tempOutputStream = Files.newOutputStream(tempFile)) {
                ByteStreams.copy(decryptedStream, tempOutputStream);
            }

            // 使用解密后的临时文件提取文本
            if (MimeType.TEXT_PLAIN.equals(mimeType)) {
                String content = new String(Files.readAllBytes(tempFile), StandardCharsets.UTF_8);
                if (content.trim().isEmpty()) {
                    throw new IOException("Text file is empty or contains only whitespace");
                }
                return content;
            } else if (MimeType.APPLICATION_PDF.equals(mimeType)) {
                return extractPdfText(tempFile.toFile());
            } else if (MimeType.OFFICE_DOCUMENT.equals(mimeType)) {
                return extractDocxText(tempFile.toFile());
            }
            throw new IOException("Unsupported MIME type: " + mimeType);
        } catch (Exception e) {
            log.error("Error extracting text from file: " + file.getName(), e);
            throw new IOException("Error extracting text: " + e.getMessage(), e);
        }
    }

    private static String extractPdfText(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            if (text == null || text.trim().isEmpty()) {
                log.warn("No text content found in PDF file: {}", file.getName());
                throw new IOException("Could not extract text from PDF file");
            }
            
            return text;
        } catch (Exception e) {
            log.error("Error reading PDF file: " + file.getName(), e);
            throw new IOException("Error reading PDF file: " + e.getMessage(), e);
        }
    }

    private static String extractDocxText(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {
            StringBuilder sb = new StringBuilder();
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            if (paragraphs.isEmpty()) {
                log.warn("No paragraphs found in DOCX file");
            }
            for (XWPFParagraph para : paragraphs) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text).append("\n");
                }
            }
            String result = sb.toString();
            if (result.trim().isEmpty()) {
                log.warn("No text content extracted from DOCX file");
            }
            return result;
        } catch (IOException e) {
            throw new IOException("Error reading DOCX file: " + e.getMessage(), e);
        }
    }

    private static String extractTxtText(File file) throws IOException {
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            if (content.trim().isEmpty()) {
                log.warn("TXT file is empty or contains only whitespace");
            }
            return content;
        } catch (IOException e) {
            throw new IOException("Error reading TXT file: " + e.getMessage(), e);
        }
    }
}

