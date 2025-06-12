package com.mybank.service;

import com.mybank.model.Alert;
import com.mybank.model.User;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class AlertService {

    @PersistenceContext(unitName = "mybankPU")
    private EntityManager em;

    public void createAlert(Alert alert) {
        em.persist(alert);
    }

    public List<Alert> getUnreadAlertsForUser(User user) {
        return em.createQuery("SELECT a FROM Alert a WHERE a.targetUser = :user AND a.read = false ORDER BY a.createdAt DESC", Alert.class)
                 .setParameter("user", user)
                 .getResultList();
    }

    public void markAllAsRead(User user) {
        em.createQuery("UPDATE Alert a SET a.read = true WHERE a.targetUser = :user AND a.read = false")
          .setParameter("user", user)
          .executeUpdate();
    }

    public List<Alert> getAllAlertsForUser(User user) {
        return em.createQuery("SELECT a FROM Alert a WHERE a.targetUser = :user ORDER BY a.createdAt DESC", Alert.class)
                 .setParameter("user", user)
                 .getResultList();
    }
}
