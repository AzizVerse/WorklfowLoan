package com.mybank.controller;

import com.mybank.model.Agency;
import com.mybank.model.Folder;
import com.mybank.model.User;
import com.mybank.service.AgencyService;
import com.mybank.service.FolderService;
import com.mybank.service.UserService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JSF managed bean - must use field injection due to JSF lifecycle requirements.
 * Constructor injection causes deployment issues (e.g. WELD-001410).
 */
@Named("agencyBean")
@SessionScoped
@SuppressWarnings("squid:S6813") // Sonar false positive: field injection is required in JSF
public class AgencyController implements Serializable {
	 private static final long serialVersionUID = 1L;
        
	// NOSONAR: Field injection is used intentionally in JSF managed beans
	    @Inject
	    private AgencyService agencyService;
	 // NOSONAR: Field injection is used intentionally in JSF managed beans
	    @Inject
	    private UserService userService;
	 // NOSONAR: Field injection is used intentionally in JSF managed beans
	    @Inject
	    private FolderService folderService;

	    // Setter Injection for testing
	    public void setAgencyService(AgencyService agencyService) {
	        this.agencyService = agencyService;
	    }

	    public void setUserService(UserService userService) {
	        this.userService = userService;
	    }

	    public void setFolderService(FolderService folderService) {
	        this.folderService = folderService;
	    }
    
    

    private String code;
    private String name;
    private Long directorId;
    private String creationMessage;

    private List<Agency> agencyList;
    private Agency selectedAgency;
    
    private List<Folder> externalFolders = new ArrayList<>();

    public List<Folder> getExternalFolders() {
        return externalFolders;
    }


    @PostConstruct
    public void init() {
        agencyList = agencyService.findAllAgencies();
    }

    public String createAgency() {
        User director = userService.findById(directorId);
        if (director == null) {
            creationMessage = "Directeur introuvable.";
            return null;
        }

        // Check if this director is already assigned
        boolean isAlreadyDirector = agencyList.stream()
                .anyMatch(a -> a.getDirector() != null && a.getDirector().getId().equals(directorId));

        if (isAlreadyDirector) {
            creationMessage = "Ce directeur est déjà affecté à une agence.";
            return null;
        }

        Agency agency = new Agency(code, name, director);
        agencyService.createAgency(agency);
        creationMessage = "Agence créée avec succès.";
        clearForm();
        agencyList = agencyService.findAllAgencies(); // refresh list
        return null;
    }

    private void clearForm() {
        code = "";
        name = "";
        directorId = null;
    }

    public List<User> getAvailableDirectors() {
        List<User> allDirectors = userService.findUsersByRole("DIRECTEUR_AGENCE");
        // filter out already-assigned ones
        return allDirectors.stream()
                .filter(director -> agencyList.stream()
                        .noneMatch(a -> a.getDirector() != null && a.getDirector().getId().equals(director.getId())))
                .toList();
    }

    public List<Agency> getAllAgencies() {
        return agencyList;
    }
    public List<Agency> getAgencies() {
        return agencyList;
    }
    
    

    
    public Agency getSelectedAgency() {
        return selectedAgency;
    }

    public void setSelectedAgency(Agency selectedAgency) {
        this.selectedAgency = selectedAgency;
    }

    
    public long folderCountForAnalyst(User analyst) {
        if (analyst == null) return 0;
        return folderService.getFolderCountForAnalyst(analyst); // Already filtered by !TERMINE
    }
    public long folderCountForCICAnalyst(User cicAnalyst) {
        if (cicAnalyst == null) return 0;
        return folderService.getFolderCountForCICAnalyst(cicAnalyst); // à implémenter dans service
    }
    public long externalFolderCountForCIC(User cicAnalyst, Agency agency) {
        if (cicAnalyst == null || agency == null) return 0;
        return folderService.countExternalCICFolders(cicAnalyst, agency); // à implémenter dans service
    }
    public void loadExternalCICOrigins(User cicAnalyst, Agency agency) {
        originAgencyNames = folderService.getOriginAgencyNamesForCIC(cicAnalyst, agency);
        externalFolders = folderService.getExternalFoldersForCICAnalyst(cicAnalyst, agency);
    }


    public long externalFolderCount(User analyst, Agency agency) {
        if (analyst == null || agency == null) return 0;
        return folderService.countFoldersFromOtherAgencies(analyst, agency); // Already filtered by !TERMINE
    }

    private List<String> originAgencyNames = new ArrayList<>();

    public void loadExternalOrigins(User analyst, Agency agency) {
        originAgencyNames = folderService.getOriginAgencyNamesForExternalFolders(analyst, agency);
        externalFolders = folderService.getExternalFoldersForAnalyst(analyst, agency);
    }

    public List<String> getOriginAgencyNames() {
        return originAgencyNames;
    }
    public List<Folder> getExternalFoldersForAnalyst(User analyst, Agency agency) {
        if (analyst == null || agency == null) return List.of();
        return folderService.getExternalFoldersForAnalyst(analyst, agency);
    }


    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getDirectorId() { return directorId; }
    public void setDirectorId(Long directorId) { this.directorId = directorId; }

    public String getCreationMessage() { return creationMessage; }
    public void setCreationMessage(String creationMessage) { this.creationMessage = creationMessage; }
}
