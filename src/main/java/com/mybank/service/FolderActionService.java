package com.mybank.service;

import java.util.List;

import com.mybank.model.Folder;
import com.mybank.model.FolderAction;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class FolderActionService {

    @PersistenceContext(unitName = "mybankPU")
    private EntityManager em;

    public void saveAction(FolderAction action) {
        em.persist(action);
    }
    public List<FolderAction> getActionsByFolder(Folder folder) {
        Folder managedFolder = em.find(Folder.class, folder.getId()); // reattach entity
        TypedQuery<FolderAction> query = em.createQuery(
            "SELECT fa FROM FolderAction fa WHERE fa.folder = :folder ORDER BY fa.actionDate DESC",
            FolderAction.class
        );
        query.setParameter("folder", managedFolder);
        return query.getResultList();
    }

}
