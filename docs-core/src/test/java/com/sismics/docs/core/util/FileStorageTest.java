package com.sismics.docs.core.util;
import org.junit.Test;

import com.sismics.docs.core.util.DocumentTextExtractor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;

import static org.junit.Assert.*;

public class FileStorageTest {
    @Test
    public void testFileStorageAndRead() throws Exception {
        // 1. 创建临时文件并写入内容
        String content = "Hello, this is a test document.";
        Path tempFile = Files.createTempFile("testdoc", ".txt");
        Files.write(tempFile, content.getBytes());

        // 2. 用 DocumentTextExtractor 读取内容
        String extracted = com.sismics.docs.core.util.DocumentTextExtractor.extractText(tempFile.toFile());
        assertEquals(content, extracted.trim());

        // 3. 删除临时文件
        Files.deleteIfExists(tempFile);
    }
}