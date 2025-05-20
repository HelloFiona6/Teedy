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

@Path("/document")
public class DocumentTranslationResource extends BaseResource {

    /**
     * 自动提取文档内容并翻译
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/translate/auto")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response autoTranslate(
            @PathParam("id") String documentId,
            @FormParam("fileId") String fileId,
            @FormParam("lang") String targetLang,
            @FormParam("userId") String userId,
            @FormParam("share") String shareId) throws Exception {
        
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // 验证参数
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new ClientException("ValidationError", "请选择要翻译的文件");
        }
        if (targetLang == null || targetLang.trim().isEmpty()) {
            throw new ClientException("ValidationError", "请选择目标语言");
        }
        System.out.println("auto: pass valid");
        
        // 获取文档
        DocumentDao documentDao = new DocumentDao();
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, getTargetIdList(shareId));
        if (documentDto == null) {
            throw new NotFoundException("Document not found");
        }
        System.out.println("auto: pass doc");

        // 获取指定的文件
        FileDao fileDao = new FileDao();
        File file = fileDao.getFile(fileId);
        if (file == null || !file.getDocumentId().equals(documentId)) {
            throw new NotFoundException("File not found in document");
        }
        checkFileAccessible(shareId, file);
        System.out.println("auto: pass get file");
        
        try {
            // 获取文件内容
            String content;
            java.nio.file.Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());
            if (!Files.exists(storedFile)) {
                throw new ClientException("FileNotFound", "File not found in storage");
            }

            // 创建临时文件
            java.nio.file.Path tempFile = AppContext.getInstance().getFileService().createTemporaryFile();
            
            // 获取文件创建者
            UserDao userDao = new UserDao();
            User user = userDao.getById(file.getUserId());
            if (user == null) {
                throw new ClientException("UserNotFound", "File creator not found");
            }

            // 解密文件到临时文件
            try (InputStream fileInputStream = Files.newInputStream(storedFile);
                 InputStream decryptedStream = EncryptionUtil.decryptInputStream(fileInputStream, user.getPrivateKey());
                 OutputStream tempOutputStream = Files.newOutputStream(tempFile)) {
                ByteStreams.copy(decryptedStream, tempOutputStream);
            }

            // 从临时文件提取内容
            content = DocumentTextExtractor.extractText(tempFile.toFile(), file.getMimeType(), file.getUserId());

            if (content == null || content.trim().isEmpty()) {
                throw new ClientException("EmptyContent", "No text content could be extracted from the file. The file might be empty or contain only images.");
            }
            
            System.out.println("auto: pass content extraction, content length=" + content.length());
            
            // 调用腾讯翻译
            String secretId = System.getenv("TENCENT_SECRET_ID");   ;
            String secretKey = System.getenv("TENCENT_SECRET_KEY");
            String translatedText = TencentTranslateUtil.translate(content, "auto", targetLang, secretId, secretKey);
            
            // 构建响应
            JsonObjectBuilder response = Json.createObjectBuilder()
                .add("translatedText", translatedText);
            
            return Response.ok(response.build().toString()).build();
            
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (errorMessage.contains("Unsupported file type")) {
                throw new ClientException("UnsupportedFileType", errorMessage);
            } else if (errorMessage.contains("Invalid PDF")) {
                throw new ClientException("InvalidPDF", "The PDF file appears to be corrupted or invalid");
            } else {
                System.err.println("Error extracting text: " + errorMessage);
                throw new ClientException("ExtractionError", "Error extracting text from file: " + errorMessage);
            }
        } catch (TencentCloudSDKException e) {
            throw new ClientException("TranslateError", e.getMessage());
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
