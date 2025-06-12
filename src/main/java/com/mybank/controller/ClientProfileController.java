package com.mybank.controller;

import com.mybank.model.ClientProfile;
import com.mybank.model.Folder;
import com.mybank.service.ClientProfileService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("clientProfileController")
@SessionScoped
public class ClientProfileController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FolderController folderBean; // to get the selected Folder

    @Inject
    private ClientProfileService clientProfileService;

    private ClientProfile clientProfile = new ClientProfile();

    // Load existing profile or prepare new
    public void loadProfile() {
        Folder selectedFolder = folderBean.getSelectedFolder();
        if (selectedFolder == null) return;

        ClientProfile existing = clientProfileService.findByFolder(selectedFolder);

        if (existing != null) {
            this.clientProfile = existing;
        } else {
            this.clientProfile = new ClientProfile();
            this.clientProfile.setFolder(selectedFolder);
        }
    }

    // Save or update
    public void saveProfile() {
        if (clientProfile == null || clientProfile.getFolder() == null) return;

        ClientProfile existing = clientProfileService.findByFolder(clientProfile.getFolder());

        if (existing == null) {
            clientProfileService.save(clientProfile);
        } else {
            clientProfile.setId(existing.getId()); // ensure we update, not duplicate
            clientProfileService.update(clientProfile);
        }

        folderBean.reloadSelectedFolder(); // optional: reload folder details if needed
    }

    // Getters and Setters
    public ClientProfile getClientProfile() {
        return clientProfile;
    }

    public void setClientProfile(ClientProfile clientProfile) {
        this.clientProfile = clientProfile;
    }
    
    public void updateValeurNette() {
        Double actifs = clientProfile.getActifsTotaux();
        Double passifs = clientProfile.getPassifsTotaux();

        if (actifs != null && passifs != null) {
            clientProfile.setValeurNette(actifs - passifs);
        } else {
            clientProfile.setValeurNette(null);
        }
    }
    
    

}
