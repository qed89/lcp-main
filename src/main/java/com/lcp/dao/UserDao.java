package com.lcp.dao;

import com.lcp.model.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;

import org.mindrot.jbcrypt.BCrypt;

public class UserDao {
    private EntityManagerFactory emf;

    public UserDao() {
        this.emf = Persistence.createEntityManagerFactory("my-persistence-unit");
    }

    public UserDao(EntityManagerFactory emf) {
        this.emf = emf;
    }
    
    public void save(User user) throws PersistenceException {
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            // Хешируем пароль перед сохранением
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            em.persist(user);

            em.getTransaction().commit();
        } catch (PersistenceException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new PersistenceException("Ошибка при сохранении пользователя: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        
        try {
            return em
                .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getSingleResult();
        } catch (NoResultException e) {
            return null; // Пользователь не найден
        } finally {
            em.close();
        }
    }

    public boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}