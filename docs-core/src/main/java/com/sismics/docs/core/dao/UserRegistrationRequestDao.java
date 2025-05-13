package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.UserRegistrationRequest;
//import com.sismics.docs.core.model.jpa.*;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * DAO for user registration requests.
 */
public class UserRegistrationRequestDao {
    /**
     * 创建注册请求
     */
    public String create(UserRegistrationRequest req) {
        req.setId(UUID.randomUUID().toString());
        req.setCreateDate(new Date());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(req);
        return req.getId();
    }

    /**
     * Get a registration request by ID.
     */
    public UserRegistrationRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(UserRegistrationRequest.class, id);
    }

    /**
     * List all pending requests.
     */
    @SuppressWarnings("unchecked")
    public List<UserRegistrationRequest> listPending() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.createQuery("from UserRegistrationRequest where status = :status order by createDate desc")
                 .setParameter("status", "pending")
                 .getResultList();
    }

    /**
     * Update a registration request (status, comment, etc)。
     */
    public void update(UserRegistrationRequest req) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.merge(req);
    }

    /**
     * Find by username (to check for duplicate requests).
     */
    public UserRegistrationRequest findByUsername(String username) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            return (UserRegistrationRequest) em.createQuery("from UserRegistrationRequest where username = :username")
                .setParameter("username", username)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
} 