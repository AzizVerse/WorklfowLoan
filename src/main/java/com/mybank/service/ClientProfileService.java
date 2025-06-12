package com.mybank.service;

import com.mybank.model.ClientProfile;
import com.mybank.model.Folder;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class ClientProfileService {

	@PersistenceContext
    private EntityManager em;

    // Save a new profile
    public void save(ClientProfile clientProfile) {
        em.persist(clientProfile);
    }

    // Update an existing profile
    public void update(ClientProfile clientProfile) {
        em.merge(clientProfile);
        em.flush();
    }

    // Find a profile by its associated folder
    public ClientProfile findByFolder(Folder folder) {
        try {
            TypedQuery<ClientProfile> query = em.createQuery(
                "SELECT cp FROM ClientProfile cp WHERE cp.folder = :folder", 
                ClientProfile.class
            );
            query.setParameter("folder", folder);
            return query.getSingleResult();
        } catch (Exception e) {
            return null; // Not found = no profile created yet
        }
    }

    // Optional: List all profiles (for admin)
    public List<ClientProfile> findAll() {
        return em.createQuery("SELECT cp FROM ClientProfile cp", ClientProfile.class).getResultList();
    }
}
