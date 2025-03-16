package com.lcp.dao;

import com.lcp.model.Form;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class FormDao {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");

    public void save(Form form) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(form);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при сохранении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public Form getFormById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Form.class, id);
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public List<Form> getFormsByUser(Long userId, int page, int pageSize) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Form> query = em.createQuery(
                "SELECT f FROM Form f WHERE f.user.id = :userId ORDER BY f.createdDate DESC", Form.class);
            query.setParameter("userId", userId);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении списка форм: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void update(Form form) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(form);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при обновлении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    
    public void delete(UUID id) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Form form = em.find(Form.class, id);
            if (form != null) {
                em.remove(form);
            }
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при удалении формы: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public long countFormsByUser(Long userId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(f) FROM Form f WHERE f.user.id = :userId", Long.class);
        query.setParameter("userId", userId);
        long count = query.getSingleResult();
        em.close();
        return count;
    }
}