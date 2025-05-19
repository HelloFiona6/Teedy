package com.sismics.docs.core.model.jpa;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Document translation entity.
 */
@Entity
@Table(name = "T_DOCUMENT_TRANSLATION")
public class DocumentTranslation {
    @Id
    @Column(name = "DTR_ID_C", length = 36)
    private String id;

    @Column(name = "DOC_ID_C", length = 36, nullable = false)
    private String documentId;

    @Column(name = "LANG_C", length = 10, nullable = false)
    private String lang;

    @Lob
    @Column(name = "TRANSLATED_TEXT_C", nullable = false)
    private String translatedText;

    @Column(name = "USER_ID_C", length = 36)
    private String userId;

    @Column(name = "CREATE_DATE_D", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;


    public String getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getLang() {
        return lang;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public String getUserId() {
        return userId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
