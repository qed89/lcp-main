package com.lcp.dao;

import com.lcp.model.Element;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.UUID;

public class ElementDao {
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("my-persistence-unit");

    public void save(Element element) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(element);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при сохранении элемента: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Element getElementById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Element.class, id);
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении элемента: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Element> getElementsByUser(Long userId, int page, int pageSize) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Element> query = em.createQuery(
                "SELECT e FROM Element e WHERE e.user.id = :userId ORDER BY e.name", Element.class);
            query.setParameter("userId", userId);
            query.setFirstResult(page * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new PersistenceException("Ошибка при получении списка элементов: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void update(Element element) throws PersistenceException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(element);
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при обновлении элемента: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public long countElementsByUser(Long userId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(e) FROM Element e WHERE e.user.id = :userId", Long.class);
        query.setParameter("userId", userId);
        long count = query.getSingleResult();
        em.close();
        return count;
    }
}