package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.DocumentTranslation;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * DAO for document translations.
 */
public class DocumentTranslationDao {
    public String create(DocumentTranslation translation) {
        translation.setId(UUID.randomUUID().toString());
        translation.setCreateDate(new Date());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(translation);
        return translation.getId();
    }

    public DocumentTranslation getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(DocumentTranslation.class, id);
    }

    public List<DocumentTranslation> listByDocumentAndLang(String documentId, String lang) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        TypedQuery<DocumentTranslation> query = em.createQuery(
            "SELECT t FROM DocumentTranslation t WHERE t.documentId = :docId AND t.lang = :lang",
            DocumentTranslation.class
        );
        query.setParameter("docId", documentId);
        query.setParameter("lang", lang);
        return query.getResultList();
    }
}
