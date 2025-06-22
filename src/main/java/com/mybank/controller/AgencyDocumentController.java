package com.mybank.controller;

import com.mybank.model.AgencyDocument;
import com.mybank.model.Alert;
import com.mybank.model.Folder;
import com.mybank.model.User;
import com.mybank.service.AgencyDocumentService;
import com.mybank.service.AlertService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.DefaultStreamedContent;
import java.io.ByteArrayInputStream;
/**
 * JSF managed bean - must use field injection due to JSF lifecycle requirements.
 * Constructor injection causes deployment issues (e.g. WELD-001410).
 */
@Named("agencyDocumentController")
@SessionScoped
@SuppressWarnings("squid:S6813") // Sonar false positive: field injection is required in JSF
public class AgencyDocumentController implements Serializable {

    private static final long serialVersionUID = 1L;
 // NOSONAR: Field injection is used intentionally in JSF managed beans
    @Inject
    private AgencyDocumentService agencyDocumentService;
 // NOSONAR: Field injection is used intentionally in JSF managed beans
    @Inject
    private FolderController folderController;
 // NOSONAR: Field injection is used intentionally in JSF managed beans
    @Inject
    private AlertService alertService;
 // NOSONAR: Field injection is used intentionally in JSF managed beans
    @Inject
    private UserController userController;


    private transient UploadedFile uploadedFile;
    private String customFileName;
    private String uploadMessage;

    public void upload() {
        Folder folder = folderController.getSelectedFolder();

        if (uploadedFile == null || folder == null || customFileName == null || customFileName.trim().isEmpty()) {
            uploadMessage = "❌ Veuillez saisir un nom de fichier et sélectionner un fichier.";
            return;
        }

        try {
            AgencyDocument doc = new AgencyDocument();
            doc.setFileName(customFileName.trim());
            doc.setUploadDate(LocalDate.now());
            doc.setFileData(uploadedFile.getContent());
            doc.setFolder(folder);

            agencyDocumentService.save(doc);
            uploadMessage = "✅ Fichier ajouté avec succès.";

            // Notify the other user
            User uploader = userController.getCurrentUser();
            User folderCreator = folder.getCreatedBy();
            User director = folderCreator.getAgency().getDirector();

            User target;
            String alertMessage;

            if (uploader.getUsername().equals(director.getUsername())) {
                // Director uploaded → Notify the chargé
                target = folderCreator;
                alertMessage = "📎 Le Directeur a ajouté un fichier au dossier : " + folder.getReference();
            } else {
                // Chargé uploaded → Notify the director
                target = director;
                alertMessage = "📎 Le chargé " + uploader.getUsername() +
                               " a ajouté un fichier au dossier : " + folder.getReference();
            }

            Alert alert = new Alert();
            alert.setTargetUser(target);
            alert.setMessage(alertMessage);
            alertService.createAlert(alert);

            // Clear fields
            uploadedFile = null;
            customFileName = null;

        } catch (Exception e) {
            uploadMessage = "❌ Erreur lors de l'envoi du fichier.";
            
        }
    }

    
    public StreamedContent download(AgencyDocument doc) {
        return DefaultStreamedContent.builder()
                .name(doc.getFileName())
                .contentType("application/octet-stream") // you can adjust if needed
                .stream(() -> new ByteArrayInputStream(doc.getFileData()))
                .build();
    }



    public List<AgencyDocument> getDocumentsForSelectedFolder() {
        Folder folder = folderController.getSelectedFolder();
        if (folder != null) {
            return agencyDocumentService.findByFolder(folder);
        }
        return List.of();
    }

    // --- Getters and Setters ---
    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getCustomFileName() {
        return customFileName;
    }

    public void setCustomFileName(String customFileName) {
        this.customFileName = customFileName;
    }

    public String getUploadMessage() {
        return uploadMessage;
    }

    public void setUploadMessage(String uploadMessage) {
        this.uploadMessage = uploadMessage;
    }
}
