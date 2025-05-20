package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.*;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.DocumentTranslation;
import com.sismics.docs.core.util.DocumentTextExtractor;
import com.sismics.docs.core.util.TencentTranslateUtil;
import com.sismics.docs.core.util.DirectoryUtil;
import com.sismics.docs.core.model.context.AppContext;
import com.sismics.rest.exception.ClientException;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.util.ValidationUtil;
import com.sismics.docs.core.model.jpa.User;
import com.sismics.docs.core.util.EncryptionUtil;
import com.google.common.io.ByteStreams;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;

@Path("/document")
public class DocumentTranslationResource extends BaseResource {

    /**
     * 自动提取文档内容并翻译
     */
    @POST
    @Path("autoTranslate")
    public Response autoTranslate(
            @FormParam("fileId") String fileId,
            @FormParam("targetLang") String targetLang,
            @FormParam("share") String shareId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // 验证参数
        if (StringUtils.isEmpty(fileId)) {
            throw new ClientException("ValidationError", "请选择要翻译的文件");
        }
        if (StringUtils.isEmpty(targetLang)) {
            throw new ClientException("ValidationError", "请选择目标语言");
        }

        // 获取文件
        File file = findFile(fileId, shareId);
        if (file == null) {
            throw new ClientException("FileNotFound", "文件不存在");
        }

        // 获取文件创建者
        UserDao userDao = new UserDao();
        User user = userDao.getById(file.getUserId());
        if (user == null) {
            throw new ClientException("UserNotFound", "文件创建者不存在");
        }

        // 解密文件内容
        String content;
        try (InputStream fileInputStream = Files.newInputStream(DirectoryUtil.getStorageDirectory().resolve(fileId));
             InputStream decryptedStream = EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey())) {
            content = new String(ByteStreams.toByteArray(decryptedStream), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ClientException("DecryptionError", "解密文件内容失败", e);
        }

        // 调用腾讯云翻译API
        try {
            String translatedText = TencentTranslateUtil.translate(content, "auto", targetLang, System.getenv("TENCENT_SECRET_ID"), System.getenv("TENCENT_SECRET_KEY"));
            return Response.ok(Json.createObjectBuilder()
                    .add("translatedText", translatedText)
                    .build())
                    .build();
        } catch (Exception e) {
            throw new ClientException("TranslationError", "翻译失败: " + e.getMessage(), e);
        }
    }

    /**
     * Find a file with access rights checking.
     *
     * @param fileId File ID
     * @param shareId Share ID
     * @return File
     */
    private File findFile(String fileId, String shareId) {
        FileDao fileDao = new FileDao();
        File file = fileDao.getFile(fileId);
        if (file == null) {
            throw new NotFoundException();
        }
        checkFileAccessible(shareId, file);
        return file;
    }
    
   
    /**
     * Check if a file is accessible to the current user
     * @param shareId Share ID
     * @param file
     */
    private void checkFileAccessible(String shareId, File file) {
        if (file.getDocumentId() == null) {
            // It's an orphan file
            if (!file.getUserId().equals(principal.getId())) {
                // But not ours
                throw new ForbiddenClientException();
            }
        } else {
            // Check document accessibility
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(file.getDocumentId(), PermType.READ, getTargetIdList(shareId))) {
                throw new ForbiddenClientException();
            }
        }
    }

    /**
     * 查询译文
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTranslation(
            @PathParam("id") String documentId,
            @QueryParam("lang") String lang
    ) {
        if (lang == null) {
            throw new ClientException("ValidationError", "lang is required");
        }
        DocumentTranslationDao dao = new DocumentTranslationDao();
        List<DocumentTranslation> list = dao.listByDocumentAndLang(documentId, lang);
        if (list.isEmpty()) {
            throw new ClientException("NotFound", "No translation found for this document and language");
        }
        DocumentTranslation translation = list.get(0);
        JsonObjectBuilder resp = Json.createObjectBuilder()
                .add("id", translation.getId())
                .add("lang", translation.getLang())
                .add("translatedText", translation.getTranslatedText());
        return Response.ok(resp.build()).build();
    }
}
