package com.mybank.service;

import com.mybank.model.DemandeCredit;
import com.mybank.model.Folder;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

@Stateless
public class DemandeCreditService {

    @PersistenceContext
    private EntityManager em;

    public void save(DemandeCredit demandeCredit) {
        em.persist(demandeCredit);
    }

    public DemandeCredit findByFolder(Folder folder) {
        try {
            return em.createQuery(
                "SELECT d FROM DemandeCredit d WHERE d.folder = :folder", DemandeCredit.class)
                .setParameter("folder", folder)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void update(DemandeCredit demandeCredit) {
        em.merge(demandeCredit);
    }


    public Folder reloadFolder(Folder folder) {
        return em.find(Folder.class, folder.getId());
    }
}
