package com.mybank.service;

import com.mybank.model.Agency;
import com.mybank.model.Department;
import com.mybank.model.Folder;
import com.mybank.model.FolderNavigation;
import com.mybank.model.Role;
import com.mybank.model.User;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@Stateless
public class FolderService {

    @PersistenceContext(unitName = "mybankPU")
    private EntityManager em;

    public void createFolder(Folder folder) {
        em.persist(folder);
    }

    public Folder findByReference(String reference) {
        try {
            return em.createQuery("SELECT f FROM Folder f WHERE f.reference = :ref", Folder.class)
                     .setParameter("ref", reference)
                     .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Folder> getFoldersByUser(User user) {
        System.out.println("📥 Querying folders for user ID: " + user.getId());
        return em.createQuery("SELECT f FROM Folder f WHERE f.createdBy.id = :userId", Folder.class)
                 .setParameter("userId", user.getId())
                 .getResultList();
    }
    public void updateFolder(Folder folder) {
        System.out.println("🔁 Updating folder: " + folder.getReference());
        System.out.println("✅ Assigned to: " + (folder.getAssignedTo() != null ? folder.getAssignedTo().getUsername() : "null"));

        em.merge(folder);
    }

    public List<Folder> getFoldersForDirector(User director) {
        return em.createQuery(
            "SELECT f FROM Folder f WHERE f.createdBy.agency.director = :director AND f.navigation != :termine", Folder.class)
            .setParameter("director", director)
            .setParameter("termine", FolderNavigation.TERMINE)
            .getResultList();
    }
    
    public Folder findById(Long id) {
        return em.find(Folder.class, id);
    }
    
 // ✅ NEW: Get all folders assigned to a specific analyst
    public List<Folder> getFoldersAssignedToUser(User analyst) {
        return em.createQuery(
            "SELECT f FROM Folder f WHERE f.assignedTo = :analyst", Folder.class)
            .setParameter("analyst", analyst)
            .getResultList();
    }

    // ✅ NEW: Assign a folder to an analyst
    public void assignFolderToAnalyst(Folder folder, User analyst) {
        folder.setAssignedTo(analyst);
        em.merge(folder);
    }
    
    
    public List<Folder> getFoldersAssignedToAnalyst(User analyst) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedTo = :analyst AND f.navigation != :termine ", Folder.class)
                 .setParameter("analyst", analyst)
                 .setParameter("termine", FolderNavigation.TERMINE)
                 .getResultList();
    }

    public List<Folder> getAllFoldersAssignedToAnalysts() {
    	return em.createQuery(
    		    "SELECT f FROM Folder f WHERE f.assignedTo IS NOT NULL AND f.assignedTo.role = :role AND f.assignedTo.department = :dep AND f.navigation != :termine ", Folder.class)
    		    .setParameter("role", Role.Analyste)
    		    .setParameter("dep", Department.ENG)
    		    .setParameter("termine", FolderNavigation.TERMINE)
    		    .getResultList();

    }
    public List<Folder> getAllFoldersAssignedToCICAnalysts() {
    	return em.createQuery(
    		    "SELECT f FROM Folder f WHERE f.assignedCIC IS NOT NULL AND f.assignedCIC.role = :role AND f.assignedCIC.department = :dep AND f.navigation != :termine ", Folder.class)
    		    .setParameter("role", Role.Analyste_CIC)
    		    .setParameter("dep", Department.CIC)
    		    .setParameter("termine", FolderNavigation.TERMINE)
    		    .getResultList();

    }
    
    public List<Folder> getAllFolders() {
        return em.createQuery("SELECT f FROM Folder f", Folder.class).getResultList();
    }
    
    public long getFolderCountForAnalyst(User analyst) {
        return em.createQuery(
            "SELECT COUNT(f) FROM Folder f WHERE f.assignedTo = :analyst AND f.navigation != :termine", Long.class)
            .setParameter("analyst", analyst)
            .setParameter("termine", FolderNavigation.TERMINE)
            .getSingleResult();
    }
    public long getFolderCountForCICAnalyst(User cicAnalyst) {
        return em.createQuery("SELECT COUNT(f) FROM Folder f WHERE f.assignedCIC = :user AND f.navigation != 'TERMINE'", Long.class)
                 .setParameter("user", cicAnalyst)
                 .getSingleResult();
    }
    public long countExternalCICFolders(User cicAnalyst, Agency agency) {
        return em.createQuery("SELECT COUNT(f) FROM Folder f WHERE f.assignedCIC = :user AND f.createdBy.agency != :agency AND f.navigation != 'TERMINE'", Long.class)
                 .setParameter("user", cicAnalyst)
                 .setParameter("agency", agency)
                 .getSingleResult();
    }
    public List<String> getOriginAgencyNamesForCIC(User cicAnalyst, Agency agency) {
        return em.createQuery("SELECT DISTINCT f.createdBy.agency.name FROM Folder f WHERE f.assignedCIC = :user AND f.createdBy.agency != :agency", String.class)
                 .setParameter("user", cicAnalyst)
                 .setParameter("agency", agency)
                 .getResultList();
    }
    public List<Folder> getExternalFoldersForCICAnalyst(User cicAnalyst, Agency agency) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedCIC = :user AND f.createdBy.agency != :agency AND f.navigation != 'TERMINE'", Folder.class)
                 .setParameter("user", cicAnalyst)
                 .setParameter("agency", agency)
                 .getResultList();
    }



    public long countFoldersFromOtherAgencies(User analyst, Agency agency) {
        return em.createQuery(
            "SELECT COUNT(f) FROM Folder f WHERE f.assignedTo = :analyst AND f.createdBy.agency != :agency AND f.navigation != :termine", Long.class)
            .setParameter("analyst", analyst)
            .setParameter("agency", agency)
            .setParameter("termine", FolderNavigation.TERMINE)
            .getSingleResult();
    }
    public List<String> getOriginAgencyNamesForExternalFolders(User analyst, Agency agency) {
        return em.createQuery(
            "SELECT DISTINCT f.createdBy.agency.name FROM Folder f " +
            "WHERE f.assignedTo = :analyst AND f.createdBy.agency != :agency AND f.navigation != :termine", String.class)
            .setParameter("analyst", analyst)
            .setParameter("agency", agency)
            .setParameter("termine", FolderNavigation.TERMINE)
            .getResultList();
    }
    public List<Folder> getExternalFoldersForAnalyst(User analyst, Agency agency) {
        return em.createQuery(
            "SELECT f FROM Folder f WHERE f.assignedTo = :analyst AND f.createdBy.agency != :agency AND f.navigation != :termine", Folder.class)
            .setParameter("analyst", analyst)
            .setParameter("agency", agency)
            .setParameter("termine", FolderNavigation.TERMINE)
            .getResultList();
    }
    
    
    public List<Folder> getFoldersAssignedToSuccursale(User succursale) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedSuc = :succ AND f.navigation != :termine", Folder.class)
                 .setParameter("succ", succursale)
                 .setParameter("termine", FolderNavigation.TERMINE)
                 .getResultList();
    }
    
    
    

    public List<Folder> getFoldersAssignedToCICAnalyst(User analyst) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedCIC = :cicanalyst AND f.navigation != :termine ", Folder.class)
                 .setParameter("cicanalyst", analyst)
                 .setParameter("termine", FolderNavigation.TERMINE)
                 .getResultList();
    }





}
