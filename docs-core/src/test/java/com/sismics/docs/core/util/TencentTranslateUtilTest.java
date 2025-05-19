package com.sismics.docs.core.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class TencentTranslateUtilTest {
    @Test
    public void testTranslate() throws Exception {
        String text = "Hello, world!";
        String secretId = "...";
        String secretKey = "...";
        String translated = TencentTranslateUtil.translate(text, "auto", "zh", secretId, secretKey);
        assertNotNull(translated);
        System.out.println("翻译结果：" + translated);
    }
}