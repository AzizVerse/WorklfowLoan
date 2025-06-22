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

    private static final String PARAM_TERMINE = "termine";
    private static final String PARAM_ANALYST = "analyst";
    private static final String PARAM_AGENCY = "agency";

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
            "SELECT f FROM Folder f WHERE f.createdBy.agency.director = :director AND f.navigation != :" + PARAM_TERMINE, Folder.class)
            .setParameter("director", director)
            .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
            .getResultList();
    }

    public Folder findById(Long id) {
        return em.find(Folder.class, id);
    }

    public List<Folder> getFoldersAssignedToUser(User analyst) {
        return em.createQuery(
            "SELECT f FROM Folder f WHERE f.assignedTo = :" + PARAM_ANALYST, Folder.class)
            .setParameter(PARAM_ANALYST, analyst)
            .getResultList();
    }

    public void assignFolderToAnalyst(Folder folder, User analyst) {
        folder.setAssignedTo(analyst);
        em.merge(folder);
    }

    public List<Folder> getFoldersAssignedToAnalyst(User analyst) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedTo = :" + PARAM_ANALYST + " AND f.navigation != :" + PARAM_TERMINE, Folder.class)
                 .setParameter(PARAM_ANALYST, analyst)
                 .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
                 .getResultList();
    }

    public List<Folder> getAllFoldersAssignedToAnalysts() {
        return em.createQuery(
            "SELECT f FROM Folder f WHERE f.assignedTo IS NOT NULL AND f.assignedTo.role = :role AND f.assignedTo.department = :dep AND f.navigation != :" + PARAM_TERMINE, Folder.class)
            .setParameter("role", Role.Analyste)
            .setParameter("dep", Department.ENG)
            .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
            .getResultList();
    }

    public List<Folder> getAllFoldersAssignedToCICAnalysts() {
        return em.createQuery(
            "SELECT f FROM Folder f WHERE f.assignedCIC IS NOT NULL AND f.assignedCIC.role = :role AND f.assignedCIC.department = :dep AND f.navigation != :" + PARAM_TERMINE, Folder.class)
            .setParameter("role", Role.Analyste_CIC)
            .setParameter("dep", Department.CIC)
            .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
            .getResultList();
    }

    public List<Folder> getAllFolders() {
        return em.createQuery("SELECT f FROM Folder f", Folder.class).getResultList();
    }

    public long getFolderCountForAnalyst(User analyst) {
        return em.createQuery(
            "SELECT COUNT(f) FROM Folder f WHERE f.assignedTo = :" + PARAM_ANALYST + " AND f.navigation != :" + PARAM_TERMINE, Long.class)
            .setParameter(PARAM_ANALYST, analyst)
            .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
            .getSingleResult();
    }

    public long getFolderCountForCICAnalyst(User cicAnalyst) {
        return em.createQuery("SELECT COUNT(f) FROM Folder f WHERE f.assignedCIC = :user AND f.navigation != 'TERMINE'", Long.class)
                 .setParameter("user", cicAnalyst)
                 .getSingleResult();
    }

    public long countExternalCICFolders(User cicAnalyst, Agency agency) {
        return em.createQuery("SELECT COUNT(f) FROM Folder f WHERE f.assignedCIC = :user AND f.createdBy.agency != :" + PARAM_AGENCY + " AND f.navigation != 'TERMINE'", Long.class)
                 .setParameter("user", cicAnalyst)
                 .setParameter(PARAM_AGENCY, agency)
                 .getSingleResult();
    }

    public List<String> getOriginAgencyNamesForCIC(User cicAnalyst, Agency agency) {
        return em.createQuery("SELECT DISTINCT f.createdBy.agency.name FROM Folder f WHERE f.assignedCIC = :user AND f.createdBy.agency != :" + PARAM_AGENCY, String.class)
                 .setParameter("user", cicAnalyst)
                 .setParameter(PARAM_AGENCY, agency)
                 .getResultList();
    }

    public List<Folder> getExternalFoldersForCICAnalyst(User cicAnalyst, Agency agency) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedCIC = :user AND f.createdBy.agency != :" + PARAM_AGENCY + " AND f.navigation != 'TERMINE'", Folder.class)
                 .setParameter("user", cicAnalyst)
                 .setParameter(PARAM_AGENCY, agency)
                 .getResultList();
    }

    public long countFoldersFromOtherAgencies(User analyst, Agency agency) {
        return em.createQuery(
            "SELECT COUNT(f) FROM Folder f WHERE f.assignedTo = :" + PARAM_ANALYST + " AND f.createdBy.agency != :" + PARAM_AGENCY + " AND f.navigation != :" + PARAM_TERMINE, Long.class)
            .setParameter(PARAM_ANALYST, analyst)
            .setParameter(PARAM_AGENCY, agency)
            .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
            .getSingleResult();
    }

    public List<String> getOriginAgencyNamesForExternalFolders(User analyst, Agency agency) {
        return em.createQuery(
            "SELECT DISTINCT f.createdBy.agency.name FROM Folder f " +
            "WHERE f.assignedTo = :" + PARAM_ANALYST + " AND f.createdBy.agency != :" + PARAM_AGENCY + " AND f.navigation != :" + PARAM_TERMINE, String.class)
            .setParameter(PARAM_ANALYST, analyst)
            .setParameter(PARAM_AGENCY, agency)
            .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
            .getResultList();
    }

    public List<Folder> getExternalFoldersForAnalyst(User analyst, Agency agency) {
        return em.createQuery(
            "SELECT f FROM Folder f WHERE f.assignedTo = :" + PARAM_ANALYST + " AND f.createdBy.agency != :" + PARAM_AGENCY + " AND f.navigation != :" + PARAM_TERMINE, Folder.class)
            .setParameter(PARAM_ANALYST, analyst)
            .setParameter(PARAM_AGENCY, agency)
            .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
            .getResultList();
    }

    public List<Folder> getFoldersAssignedToSuccursale(User succursale) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedSuc = :succ AND f.navigation != :" + PARAM_TERMINE, Folder.class)
                 .setParameter("succ", succursale)
                 .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
                 .getResultList();
    }

    public List<Folder> getFoldersAssignedToCICAnalyst(User analyst) {
        return em.createQuery("SELECT f FROM Folder f WHERE f.assignedCIC = :cicanalyst AND f.navigation != :" + PARAM_TERMINE, Folder.class)
                 .setParameter("cicanalyst", analyst)
                 .setParameter(PARAM_TERMINE, FolderNavigation.TERMINE)
                 .getResultList();
    }
}
