package com.sismics.docs.rest.resource;

import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.DocumentTranslationDao;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.DocumentTranslation;
import com.sismics.docs.core.dao.DocumentDao;
import com.sismics.docs.core.util.DocumentTextExtractor;
import com.sismics.docs.core.util.TencentTranslateUtil;
import com.sismics.rest.exception.ClientException;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.sismics.docs.core.dao.FileDao;
// import com.sismics.docs.core.model.jpa.File;
import com.sismics.rest.exception.ForbiddenClientException;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/document/{id}/translate")
public class DocumentTranslationResource extends BaseResource {

    /**
     * 自动提取文档内容并翻译
     */
    @POST
    @Path("/auto")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response autoTranslate(
            @PathParam("id") String documentId,
            @FormParam("lang") String lang,
            @FormParam("userId") String userId
    ) throws Exception {
        // 先校验登录
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // 1. 权限校验+查找文档
        DocumentDao documentDao = new DocumentDao();
        DocumentDto documentDto = documentDao.getDocument(documentId, PermType.READ, getTargetIdList(null));
        if (documentDto == null) {
            throw new NotFoundException("Document not found");
        }

        // 2. 获取主文件ID
        String fileId = documentDto.getFileId();
        if (fileId == null) {
            throw new NotFoundException("No main file for this document");
        }

        // 3. 查找主文件
        FileDao fileDao = new FileDao();
        com.sismics.docs.core.model.jpa.File fileEntity = fileDao.getFile(fileId);
        if (fileEntity == null) {
            throw new NotFoundException("Main file not found");
        }

        // 4. 获取实际文件路径
        java.io.File realFile = com.sismics.docs.core.util.DirectoryUtil.getStorageDirectory().resolve(fileId).toFile();

        // 5. 自动提取文本
        String originalText = DocumentTextExtractor.extractText(realFile);

        // 6. 如果文本为空或很短，尝试OCR
        if (originalText == null || originalText.trim().length() < 30) {
            String fileName = fileEntity.getName();
            String ocrText = "";
            if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                // PDF图片型，逐页渲染为图片后OCR
                try (org.apache.pdfbox.pdmodel.PDDocument pdf = org.apache.pdfbox.pdmodel.PDDocument.load(realFile)) {
                    org.apache.pdfbox.rendering.PDFRenderer renderer = new org.apache.pdfbox.rendering.PDFRenderer(pdf);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < pdf.getNumberOfPages(); i++) {
                        java.awt.image.BufferedImage image = renderer.renderImageWithDPI(i, 300);
                        sb.append(com.sismics.docs.core.util.FileUtil.ocrFile("eng", image));
                    }
                    ocrText = sb.toString();
                }
            } else if (fileName != null && (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".png"))) {
                // 图片文件直接OCR
                java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(realFile);
                ocrText = com.sismics.docs.core.util.FileUtil.ocrFile("eng", image);
            }
            if (ocrText != null && ocrText.trim().length() > 0) {
                originalText = ocrText;
            }
        }

        // 7. 调用腾讯翻译
        String secretId = System.getenv("TENCENT_SECRET_ID");
        String secretKey = System.getenv("TENCENT_SECRET_KEY");
        String translatedText;
        try {
            translatedText = TencentTranslateUtil.translate(originalText, "auto", lang, secretId, secretKey);
        } catch (TencentCloudSDKException e) {
            throw new ClientException("TranslateError", e.getMessage());
        }

        // 8. 保存译文
        DocumentTranslation translation = new DocumentTranslation();
        translation.setId(UUID.randomUUID().toString());
        translation.setDocumentId(documentId);
        translation.setLang(lang);
        translation.setTranslatedText(translatedText);
        translation.setUserId(userId);
        translation.setCreateDate(new Date());

        DocumentTranslationDao dao = new DocumentTranslationDao();
        dao.create(translation);

        // 9. 返回译文
        JsonObjectBuilder resp = Json.createObjectBuilder()
                .add("id", translation.getId())
                .add("lang", lang)
                .add("translatedText", translatedText);
        return Response.ok(resp.build()).build();
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
