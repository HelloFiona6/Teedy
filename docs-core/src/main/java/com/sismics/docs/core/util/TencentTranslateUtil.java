package com.sismics.docs.core.util;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;

public class TencentTranslateUtil {
    public static String translate(String text, String sourceLang, String targetLang, String secretId, String secretKey) throws TencentCloudSDKException {
        Credential cred = new Credential(secretId, secretKey);
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("tmt.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        TmtClient client = new TmtClient(cred, "ap-guangzhou", clientProfile);

        TextTranslateRequest req = new TextTranslateRequest();
        req.setSourceText(text);
        req.setSource(sourceLang); // "auto" 自动检测
        req.setTarget(targetLang); // 目标语言，如 "zh"、"en"
        req.setProjectId(0L);

        TextTranslateResponse resp = client.TextTranslate(req);
        return resp.getTargetText();
    }
}
