package com.mybank.service;

import com.mybank.model.User;
import com.mybank.model.Role;
import com.mybank.model.Agency;
import com.mybank.model.Department;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

@Stateless
public class UserService {

    @PersistenceContext(unitName = "mybankPU")
    private EntityManager em;

    public String authenticate(String username, String password) {
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                          .setParameter("username", username)
                          .getSingleResult();

            if (user != null && user.getPassword().equals(hashPassword(password))) {
                return user.getRole().name();
            }
        } catch (NoResultException e) {
            return null;
        }
        return null;
    }

    public User getUserByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                     .setParameter("username", username)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User getUserByMatricule(String matricule) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.matricule = :matricule", User.class)
                     .setParameter("matricule", matricule)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void createUser(User user) {
        
        em.persist(user);
    }

    public List<User> getUsersByRole(String roleName) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.role = :role", User.class)
                     .setParameter("role", Role.valueOf(roleName))
                     .getResultList();
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    public User getUserById(Long id) {
        return em.find(User.class, id);
    }

    public void assignUserToAgency(User user, Agency agency) {
        user.setAgency(agency);
        em.merge(user);
    }

    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public List<User> findUsersByRole(String roleName) {
        try {
            Role roleEnum = Role.valueOf(roleName); // Convert string to enum
            return em.createQuery("SELECT u FROM User u WHERE u.role = :role", User.class)
                     .setParameter("role", roleEnum)
                     .getResultList();
        } catch (IllegalArgumentException e) {
            return List.of(); // In case the role string doesn't match enum
        }
    }
    
    public List<User> getUsersByRoleAndDepartment(Role role, Department department) {
        return em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role AND u.department = :dept", User.class)
                .setParameter("role", role)
                .setParameter("dept", department)
                .getResultList();
    }
    public User getGlobalResponsibleAnalyst() {
        return em.createQuery(
            "SELECT u FROM User u WHERE u.role = :role AND u.department = :dept", User.class)
            .setParameter("role", Role.RESPONSABLE_ANALYSTE)
            .setParameter("dept", Department.ENG)
            .setMaxResults(1)
            .getSingleResult();
    }
    
    public User getGlobalDirectorENG() {
    	return em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role AND u.department = :dept", User.class)
                .setParameter("role", Role.DIRECTEUR_ENGAGEMENT)
                .setParameter("dept", Department.ENG)
                .setMaxResults(1)
                .getSingleResult(); 
    }
    
    public User getGlobalDirectorCIC() {
    	return em.createQuery(
                "SELECT u FROM User u WHERE u.role = :role AND u.department = :dept", User.class)
                .setParameter("role", Role.Directeur_CIC)
                .setParameter("dept", Department.CIC)
                .setMaxResults(1)
                .getSingleResult(); 
    }



}
