package com.mybank.service;

import com.mybank.model.AgencyDocument;
import com.mybank.model.Folder;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class AgencyDocumentService {

    @PersistenceContext(unitName = "mybankPU")
    private EntityManager em;

    public void save(AgencyDocument document) {
        em.persist(document);
    }

    public List<AgencyDocument> findByFolder(Folder folder) {
        return em.createQuery("SELECT d FROM AgencyDocument d WHERE d.folder = :folder", AgencyDocument.class)
                 .setParameter("folder", folder)
                 .getResultList();
    }

    public AgencyDocument findById(Long id) {
        return em.find(AgencyDocument.class, id);
    }

    public void delete(AgencyDocument document) {
        if (!em.contains(document)) {
            document = em.merge(document);
        }
        em.remove(document);
    }
}
