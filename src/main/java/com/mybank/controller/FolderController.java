package com.mybank.controller;

import com.mybank.model.Agency;
import com.mybank.model.Alert;
import com.mybank.model.DemandeCredit;
import com.mybank.model.Folder;
import com.mybank.model.FolderAction;
import com.mybank.model.FolderNavigation;
import com.mybank.model.Role;
import com.mybank.model.User;
import com.mybank.service.AgencyService;
import com.mybank.service.AlertService;
import com.mybank.service.ClientProfileService;
import com.mybank.service.DemandeCreditService;
import com.mybank.service.FolderActionService;
import com.mybank.service.FolderService;
import com.mybank.service.UserService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
/**
 * JSF managed bean - must use field injection due to JSF lifecycle requirements.
 * Constructor injection causes deployment issues (e.g. WELD-001410).
 */
@Named("folderBean")
@SessionScoped
@SuppressWarnings("squid:S6813") // Sonar false positive: field injection is required in JSF	
public class FolderController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FolderService folderService;
    public void setFolderService(FolderService folderService) {
        this.folderService = folderService;
    }

    @Inject
    private UserService userService;
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    private UserController userController;
    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    @Inject
    private FolderActionService folderActionService;
    public void setFolderActionService(FolderActionService folderActionService) {
        this.folderActionService = folderActionService;
    }

    @Inject
    private AlertService alertService;
    public void setAlertService(AlertService alertService) {
        this.alertService = alertService;
    }

    @Inject
    private AgencyService agencyService;
    public void setAgencyService(AgencyService agencyService) {
        this.agencyService = agencyService;
    }

    

    @Inject
    private DemandeCreditService demandeCreditService;
    public void setDemandeCreditService(DemandeCreditService demandeCreditService) {
        this.demandeCreditService = demandeCreditService;
    }

    @Inject
    private ClientProfileService clientProfileService;
    public void setClientProfileService(ClientProfileService clientProfileService) {
        this.clientProfileService = clientProfileService;
    }

    @Inject
    private ClientProfileController clientProfileController;
    public void setClientProfileController(ClientProfileController clientProfileController) {
        this.clientProfileController = clientProfileController;
    }
    
    


    private String reference;
    private String description;
    private String creationMessage;
    private String client;
    private String newComment;
    private Double revenuMensuel;
    private Double montantDemande;
    private Integer dureeMois;
    private Double tauxAnnuel;

    private transient List<Folder> myFolders;
    private transient Folder selectedFolder;
    private transient User selectedAnalyst;
    private transient List<Folder> analystFolders;
    private transient List<Folder> cicFolders;
    private transient List<Folder> foldersAssignedToMe;
    private transient List<Folder> foldersAssignedToCIC;
    private Long selectedAnalystId;
    private Long selectedCICAnalystId;
    
    private List<SelectItem> options;
    private FolderNavigation selectedStatus;

    public FolderNavigation getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(FolderNavigation selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    public Long getSelectedCICAnalystId() {
        return selectedCICAnalystId;
    }

    public void setSelectedCICAnalystId(Long selectedCICAnalystId) {
        this.selectedCICAnalystId = selectedCICAnalystId;
    }

    public Long getSelectedAnalystId() {
        return selectedAnalystId;
    }

    public void setSelectedAnalystId(Long selectedAnalystId) {
        this.selectedAnalystId = selectedAnalystId;
    }
    
    
    
    public List<SelectItem> getStatusOptions() {
        List<SelectItem> options = new ArrayList<>();
        options.add(new SelectItem(null, "Tous les statuts")); // default
        for (FolderNavigation nav : FolderNavigation.values()) {
            options.add(new SelectItem(nav, "📄 " + nav.name()));
        }
        return options;
    }
    public void assignFolderAsAdmin() {
        if (selectedFolder != null && selectedAnalystId != null) {
            User analyst = userService.getUserById(selectedAnalystId);
            if (analyst != null && analyst.getRole() == Role.Analyste) {
                selectedFolder.setAssignedTo(analyst);
                folderService.updateFolder(selectedFolder);

                FolderAction action = new FolderAction();
                action.setFolder(selectedFolder);
                action.setUser(userController.getCurrentUser());
                action.setComment("✅ Dossier assigné par l'admin à l’analyste : " + analyst.getFirstName());
                action.setActionDate(LocalDate.now());
                folderActionService.saveAction(action);

                Alert alert = new Alert();
                alert.setTargetUser(analyst);
                alert.setMessage("🆕 Un dossier vous a été assigné par l’administrateur.");
                alertService.createAlert(alert);

                

                newComment = "";
            }
        }
    }
    
    public void assignCICFolderAsAdmin() {
        if (selectedFolder != null && selectedCICAnalystId != null) {
            User analyst = userService.getUserById(selectedCICAnalystId);
            if (analyst != null && analyst.getRole() == Role.Analyste_CIC) {
                selectedFolder.setAssignedCIC(analyst);
                folderService.updateFolder(selectedFolder);

                FolderAction action = new FolderAction();
                action.setFolder(selectedFolder);
                action.setUser(userController.getCurrentUser());
                action.setComment("✅ Dossier assigné par l'admin à l’analyste : " + analyst.getFirstName());
                action.setActionDate(LocalDate.now());
                folderActionService.saveAction(action);

                Alert alert = new Alert();
                alert.setTargetUser(analyst);
                alert.setMessage("🆕 Un dossier vous a été assigné par l’administrateur.");
                alertService.createAlert(alert);

                

                newComment = "";
            }
        }
    }
    



    // 🟢 Called automatically by <f:event>
    public void loadFoldersForCurrentUser() {
        User loggedUser = userController.getCurrentUser();
        if (loggedUser != null) {
            switch (loggedUser.getRole()) {
                case DIRECTEUR_AGENCE:
                    List<Folder> ownAgencyFolders = folderService.getFoldersForDirector(loggedUser);
                    List<Folder> assignedToAsSuccursale = folderService.getFoldersAssignedToSuccursale(loggedUser);

                    ownAgencyFolders.addAll(assignedToAsSuccursale);
                    myFolders = ownAgencyFolders.stream()
                            .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                            .collect(Collectors.toList());
                        break;

                case Analyste:
                    foldersAssignedToMe = folderService.getFoldersAssignedToAnalyst(loggedUser).stream()
                        .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                        .collect(Collectors.toList());
                    myFolders = null;
                    break;
                
                case Analyste_CIC:
                	foldersAssignedToCIC = folderService.getFoldersAssignedToCICAnalyst(loggedUser).stream()
                        .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                        .collect(Collectors.toList());
                    myFolders = null;
                    break;    

                case RESPONSABLE_ANALYSTE:
                    analystFolders = folderService.getAllFoldersAssignedToAnalysts()
                        .stream()
                        .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                        .collect(Collectors.toList());
                    break;

                case DIRECTEUR_ENGAGEMENT:
                    analystFolders = folderService.getAllFoldersAssignedToAnalysts()
                        .stream()
                        .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                        .collect(Collectors.toList());
                    myFolders = null;
                    break;
                case Directeur_CIC:
                    cicFolders = folderService.getAllFoldersAssignedToCICAnalysts()
                        .stream()
                        .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                        .collect(Collectors.toList());
                    myFolders = null;
                    break;    

                case CHARGE_DOSSIER:
                    myFolders = folderService.getFoldersByUser(loggedUser).stream()
                        .filter(f -> f.getNavigation() != FolderNavigation.TERMINE)
                        .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                        .collect(Collectors.toList());
                    break;

                case ADMIN:
                    myFolders = folderService.getAllFolders().stream()
                        .sorted(Comparator.comparing(Folder::getCreatedDate).reversed())
                        .collect(Collectors.toList());
                    break;

                default:
                    myFolders = List.of();
            }
        }
    }

    
    public void sendToResponsableSuccursale() {
        User currentUser = userController.getCurrentUser();

        if (selectedFolder == null || currentUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Action non autorisée.", null));
            return;
        }

        Agency agency = selectedFolder.getCreatedBy().getAgency();
        User responsable = agency.getResponsableSuccursale();

        if (responsable == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "⚠️ Aucun Responsable Succursale défini pour cette agence.", null));
            return;
        }

        // ✅ Set navigation and assigned responsible
        selectedFolder.setNavigation(FolderNavigation.ENVOYE_AU_SUCCURSALE);
        selectedFolder.setAssignedSuc(responsable);

        // ✅ Persist update
        folderService.updateFolder(selectedFolder);

        // ✅ Log action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(currentUser);
        action.setComment("envoye a "+responsable.getFirstName()+responsable.getLastName()+": " + newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // ✅ Send alert
        Alert alert = new Alert();
        alert.setTargetUser(responsable);
        alert.setMessage("📥 Nouveau dossier reçu de l'agence: " + selectedFolder.getReference());
        alertService.createAlert(alert);

        newComment = "";

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "✅ Dossier envoyé au Responsable Succursale.", null));
    }

    public boolean hasResponsableSuccursale(Folder folder) {
        if (folder == null || folder.getCreatedBy() == null || folder.getCreatedBy().getAgency() == null) return false;
        return folder.getCreatedBy().getAgency().getResponsableSuccursale() != null;
    }
    
    
    public boolean isCurrentUserAssignedSuccursale() {
        User currentUser = userController.getCurrentUser();
        return selectedFolder != null &&
               selectedFolder.getAssignedSuc() != null &&
               selectedFolder.getAssignedSuc().getId().equals(currentUser.getId());
    }
    public boolean isCurrentUserAlsoResponsableSuccursale() {
        User current = userController.getCurrentUser();
        if (selectedFolder == null || selectedFolder.getCreatedBy() == null) return false;

        Agency agency = selectedFolder.getCreatedBy().getAgency();
        return agency != null &&
               agency.getResponsableSuccursale() != null &&
               agency.getResponsableSuccursale().getId().equals(current.getId());
    }



    public void markAsCompleted() {
        if (selectedFolder != null) {
            selectedFolder.setNavigation(FolderNavigation.TERMINE);
            folderService.updateFolder(selectedFolder);

            FolderAction action = new FolderAction();
            action.setFolder(selectedFolder);
            action.setUser(userController.getCurrentUser());
            action.setComment("📦 Dossier marqué comme terminé.");
            action.setActionDate(LocalDate.now());
            folderActionService.saveAction(action);

            // ✅ Notify the director
            User director = selectedFolder.getCreatedBy().getAgency().getDirector();
            if (director != null) {
                Alert alert = new Alert();
                alert.setTargetUser(director);
                alert.setMessage("📦 Le chargé " + userController.getCurrentUser().getUsername() +
                                 " a marqué le dossier " + selectedFolder.getReference() + " comme terminé.");
                alertService.createAlert(alert);
            }

            creationMessage = "📁 Le dossier a été marqué comme terminé.";
            newComment = "";
            selectedFolder = null; // reset view
            loadFoldersForCurrentUser(); // refresh list to remove completed ones
        }
    }



    public String createFolderAndProfileAndDemande() {
        User currentUser = userController.getCurrentUser();
        if (currentUser == null) {
            creationMessage = "Erreur : Aucun utilisateur connecté.";
            return null;
        }

        try {
            // === Step 1: Create and Save the Folder ===
            Folder folder = new Folder();
            folder.setReference(reference);
            folder.setDescription(description);
            folder.setClient(client);
            folder.setNavigation(FolderNavigation.CREE_PAR_CHARGE);
            folder.setCreatedDate(LocalDate.now());
            folder.setCreatedBy(currentUser);

            // 🧩 Fill Folder credit simulation data
            folder.setRevenuMensuel(revenuMensuel);
            folder.setMontantDemande(montantDemande);
            folder.setDureeMois(dureeMois);
            folder.setTauxAnnuel(tauxAnnuel);

            folderService.createFolder(folder); // ✅ Save Folder

            // === Step 2: Create and Save ClientProfile ===
            clientProfileController.getClientProfile().setFolder(folder);
            clientProfileController.saveProfile(); // ensures update logic is respected


            // === Step 3: Create and Save DemandeCredit ===
            DemandeCredit demandeCredit = new DemandeCredit();
            demandeCredit.setFolder(folder);

            // You can leave mensualiteEstimee, tauxEndettement, tauxEndettementAvant null at first
            // They will be calculated later when you simulate
            demandeCreditService.save(demandeCredit); // ✅ Save DemandeCredit

            // === Step 4: Notify Director ===
            User director = currentUser.getAgency().getDirector();
            if (director != null) {
                Alert alert = new Alert();
                alert.setTargetUser(director);
                alert.setMessage("📁 Un nouveau dossier a été créé par le chargé " + currentUser.getUsername());
                alertService.createAlert(alert);
            }

            creationMessage = "✅ Dossier, Profil Client, et Demande de Crédit créés avec succès.";
            clearForm(); // clean form fields
            return "officer_dashboard.xhtml?faces-redirect=true";

        } catch (Exception e) {
            creationMessage = "❌ Erreur lors de la création du dossier: " + e.getMessage();
            e.printStackTrace();
            return null;
        }
    }

    
    public void reloadSelectedFolder() {
        if (selectedFolder != null) {
            selectedFolder = folderService.findById(selectedFolder.getId());
        }
    }


    private void clearForm() {
        reference = "";
        description = "";
        client = "";
        revenuMensuel = null;
        montantDemande = null;
        dureeMois = null;
        tauxAnnuel = null;
    }



    public void selectFolder(Folder folder) {
        if (folder == null) {
            System.out.println("⚠️ [selectFolder] Called with null folder");
            return;
        }

        System.out.println("✅ [selectFolder] ID: " + folder.getId() + " | Ref: " + folder.getReference());

        if (this.selectedFolder != null && folder.getId() != null && folder.getId().equals(this.selectedFolder.getId())) {
            this.selectedFolder = null;
            System.out.println("🔁 Deselected folder.");
        } else {
            this.selectedFolder = folder;
            System.out.println("🎯 Selected folder updated.");
            clientProfileController.loadProfile();
        }
    }

    
    public void sendToResponsableAnalyste() {
        if (selectedFolder == null) return;

        User analyst = userController.getCurrentUser();
        User responsable = userService.getGlobalResponsibleAnalyst();
        // Save action with comment
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(analyst);
        action.setComment("envoyé a "+responsable.getFirstName()+" "+responsable.getLastName()+" : " + newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update folder navigation
        selectedFolder.setNavigation(FolderNavigation.ENVOYE_AU_RESPONSABLE_ANALYSTE);
        folderService.updateFolder(selectedFolder);

        // Notify Responsable Analyste (we assume only one in the system)
         // Implement this in your UserService
        if (responsable != null) {
            Alert alert = new Alert();
            alert.setTargetUser(responsable);
            alert.setMessage("📤 Le dossier '" + selectedFolder.getReference() + "' a été transmis par l’analyste " + analyst.getUsername());
            alertService.createAlert(alert);
        }

        newComment = "";
        creationMessage = "✅ Dossier transmis au Responsable Analyste.";
        System.out.println("📤 Dossier transmis à Responsable Analyste.");
    }
    
    
    public void sendToDirectorCIC() {
        if (selectedFolder == null) return;

        User analyst = userController.getCurrentUser();
        User responsable = userService.getGlobalDirectorCIC();
        // Save action with comment
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(analyst);
        action.setComment("envoyé a "+responsable.getFirstName()+" "+responsable.getLastName()+" : " + newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update folder navigation
        selectedFolder.setNavigation(FolderNavigation.ENVOYE_AU_DIRECTEUR_CIC);
        folderService.updateFolder(selectedFolder);

        
        if (responsable != null) {
            Alert alert = new Alert();
            alert.setTargetUser(responsable);
            alert.setMessage("📤 Le dossier '" + selectedFolder.getReference() + "' a été transmis par l’analyste cic " + analyst.getUsername());
            alertService.createAlert(alert);
        }

        newComment = "";
        
    }
    public void sendToDirectorEngagement() {
        if (selectedFolder == null) return;

        User responsable = userController.getCurrentUser();
        User directorEngagement = userService.getGlobalDirectorENG(); // ⬅️ using your existing method

        if (directorEngagement == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Directeur Engagement introuvable", null));
            return;
        }

        // Save folder action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(responsable);
        action.setComment("📤 Envoyé a "+directorEngagement.getFirstName()+" "+directorEngagement.getLastName()+" : " + newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update navigation
        selectedFolder.setNavigation(FolderNavigation.ENVOYE_AU_DIRECTEUR_ENGAGEMENT);
        folderService.updateFolder(selectedFolder);

        // Notify director engagement
        Alert alert = new Alert();
        alert.setTargetUser(directorEngagement);
        alert.setMessage("📩 Nouveau dossier '" + selectedFolder.getReference() + "' transmis par " + responsable.getUsername());
        alertService.createAlert(alert);

        creationMessage = "✅ Dossier envoyé au Directeur Engagement.";
        newComment = "";
    }

    
    public void returnOfferToDirector() {
        if (selectedFolder == null) return;

        User responsable = userController.getCurrentUser();
        User director = selectedFolder.getCreatedBy().getAgency().getDirector();

        if (director == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Directeur introuvable.", null));
            return;
        }

        // Save action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(responsable);
        action.setComment("Retour Offre au DA "+director.getFirstName()+" "+director.getLastName()+" : " + newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update folder status
        selectedFolder.setNavigation(FolderNavigation.RETOUR_OFFRE);
        folderService.updateFolder(selectedFolder);

        // Notify director
        Alert alert = new Alert();
        alert.setTargetUser(director);
        alert.setMessage("📤 Le Responsable Analyste " + responsable.getUsername()
            + " a retourné le dossier '" + selectedFolder.getReference() + "' pour décision finale.");
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Dossier retourné au Directeur pour offre finale.";
        System.out.println("📤 Dossier '" + selectedFolder.getReference() + "' retourné au Directeur par le Responsable Analyste.");
    }




    public void sendToDirector() {
        User currentUser = userController.getCurrentUser();
        if (selectedFolder == null || currentUser == null) return;

        User director = selectedFolder.getCreatedBy().getAgency().getDirector();
        if (director == null) {
            creationMessage = "❌ Impossible de trouver le directeur de l'agence.";
            return;
        }

        // Save action
        FolderAction action = new FolderAction(selectedFolder, currentUser, "↩️ enovyee au directeur Agence: " + newComment);
        action.setFolder(selectedFolder);
        action.setUser(currentUser);
        action.setComment("envoye a " + director.getFirstName()+" "+ director.getLastName()+" : "+newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update folder state
        selectedFolder.setNavigation(FolderNavigation.ENVOYE_AU_DIRECTEUR);
        folderService.updateFolder(selectedFolder);

        // Create alert for the director
        Alert alert = new Alert();
        alert.setTargetUser(director);
        alert.setMessage("📤 Le dossier '" + selectedFolder.getReference() + "' a été transmis par le chargé " + currentUser.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Dossier transmis au directeur de l'agence : " + director.getUsername();
        System.out.println("📤 Folder from " + currentUser.getUsername() + " sent to director: " + director.getUsername());
    }

    public void loadFoldersForDirector() {
        User currentDirector = userController.getCurrentUser();
        if (currentDirector != null && currentDirector.getRole() == Role.DIRECTEUR_AGENCE) {
            myFolders = folderService.getFoldersForDirector(currentDirector);
            System.out.println("📥 Folders loaded for Director: " + currentDirector.getUsername());
        }
    }


    public void approveFolder() {
        if (selectedFolder == null) return;

        User director = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        FolderAction action = new FolderAction(selectedFolder, director, " Approuvé par "+director.getFirstName()+" "+director.getLastName()
        +": "+ newComment);
        folderActionService.saveAction(action);

        selectedFolder.setNavigation(FolderNavigation.APPROUVE_PAR_DIRECTEUR);
        folderService.updateFolder(selectedFolder);

        // Alert to officer
        Alert alert = new Alert();
        alert.setTargetUser(officer);
        alert.setMessage("✅ Votre dossier '" + selectedFolder.getReference() + "' a été approuvé par le directeur " + director.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Le dossier a été approuvé.";
    }


    public void rejectFolder() {
        if (selectedFolder == null) return;

        User director = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        FolderAction action = new FolderAction(selectedFolder, director, " Rejeté par  " +director.getFirstName()+" "+director.getLastName()
        +": "+ newComment);
        folderActionService.saveAction(action);

        selectedFolder.setNavigation(FolderNavigation.REJETE_PAR_DIRECTEUR);
        folderService.updateFolder(selectedFolder);

        // Alert to officer
        Alert alert = new Alert();
        alert.setTargetUser(officer);
        alert.setMessage("❌ Votre dossier '" + selectedFolder.getReference() + "' a été rejeté par le directeur " + director.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "❌ Le dossier a été rejeté.";
    }


    public void returnToOfficer() {
        if (selectedFolder == null) return;

        User currentUser = userController.getCurrentUser(); // the director
        User officer = selectedFolder.getCreatedBy();

        // Save action
        FolderAction action = new FolderAction(selectedFolder, currentUser, " Retourné a " +officer.getFirstName()+" "+officer.getLastName() 
        +": "+ newComment);
        folderActionService.saveAction(action);

        // Update folder state
        selectedFolder.setNavigation(FolderNavigation.RETOURNE_AU_CHARGE);
        folderService.updateFolder(selectedFolder);

        // Send alert to the officer
        Alert alert = new Alert();
        alert.setTargetUser(officer);
        alert.setMessage("↩️ Le dossier '" + selectedFolder.getReference() + "' vous a été retourné par le directeur " + currentUser.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "🔁 Le dossier a été retourné au chargé.";
    }

    public void returnToChargeFromAnalyst() {
        if (selectedFolder == null) return;

        User analyst = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        // Save the action
        FolderAction action = new FolderAction(selectedFolder, analyst, "Retourné au "+officer.getFirstName()+" "+officer.getLastName()
        		+" par l’analyste ENG: " + newComment);
        folderActionService.saveAction(action);

        // Update folder status
        selectedFolder.setNavigation(FolderNavigation.RETOURNE_DE_Analyste);
        folderService.updateFolder(selectedFolder);

        // Notify the officer
        Alert alert = new Alert();
        alert.setTargetUser(officer);
        alert.setMessage("↩️ Le dossier '" + selectedFolder.getReference() + "' a été retourné par l’analyste " + analyst.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Dossier retourné au chargé d’agence.";
    }
    
    public void returnToChargeFromAnalystCIC() {
    	if (selectedFolder == null) return;

        User analyst = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        // Save the action
        FolderAction action = new FolderAction(selectedFolder, analyst, "Retourné au "+officer.getFirstName()+" "+officer.getLastName()
		+" par l’analyste CIC: " + newComment);
        folderActionService.saveAction(action);

        // Update folder status
        selectedFolder.setNavigation(FolderNavigation.RETOURNE_DE_AnalysteCIC);
        folderService.updateFolder(selectedFolder);

        // Notify the officer
        Alert alert = new Alert();
        alert.setTargetUser(officer);
        alert.setMessage("↩️ Le dossier '" + selectedFolder.getReference() + "' a été retourné par l’analyste " + analyst.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Dossier retourné au chargé d’agence.";
    	
    }

    public void sendToAnalystAgain() {
        User currentUser = userController.getCurrentUser();

     // Get analyst from current user's agency
        User analyst = selectedFolder.getAssignedTo();
        if (analyst == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Aucun analyste assigné par défaut.", null));
            return;
        }

        // Save action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(currentUser);
        action.setComment("renvoyé à  "+analyst.getFirstName()+" "+analyst.getLastName()+": "+ newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update folder status
        selectedFolder.setNavigation(FolderNavigation.ASSIGNE_A_ANALYSTE);
        folderService.updateFolder(selectedFolder);

        // Notify analyst
        Alert alert = new Alert();
        alert.setTargetUser(analyst);
        alert.setMessage("📥 Le dossier '" + selectedFolder.getReference() + "' vous a été renvoyé pour analyse par " + currentUser.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Dossier renvoyé à l’analyste : " + analyst.getUsername();
        System.out.println("📤 Folder '" + selectedFolder.getReference() + "' renvoyé à l’analyste " + analyst.getUsername() + " par " + currentUser.getUsername());
    }
    public void sendToAnalystCICAgain() {
        User currentUser = userController.getCurrentUser();

     // Get analyst from current user's agency
        User analyst = selectedFolder.getAssignedCIC();
        if (analyst == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Aucun analyste assigné par défaut.", null));
            return;
        }

        // Save action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(currentUser);
        action.setComment("renvoyé à "+analyst.getFirstName()+analyst.getLastName()+": "+newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update folder status
        selectedFolder.setNavigation(FolderNavigation.ASSIGNE_A_ANALYSTECIC);
        folderService.updateFolder(selectedFolder);

        // Notify analyst
        Alert alert = new Alert();
        alert.setTargetUser(analyst);
        alert.setMessage("📥 Le dossier '" + selectedFolder.getReference() + "' vous a été renvoyé pour analyse CIC par " + currentUser.getUsername());
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Dossier renvoyé à l’analyste : " + analyst.getUsername();
        System.out.println("📤 Folder '" + selectedFolder.getReference() + "' renvoyé à l’analyste " + analyst.getUsername() + " par " + currentUser.getUsername());
    }


    public List<FolderAction> getActionsForSelectedFolder() {
        if (selectedFolder == null) return List.of();

        return folderActionService.getActionsByFolder(selectedFolder).stream()
                .sorted(Comparator.comparing(FolderAction::getId)) // oldest to newest
                .collect(Collectors.toList());
    }

    public void validateDirectly() {
        if (selectedFolder == null) return;

        User director = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        selectedFolder.setNavigation(FolderNavigation.APPROUVE_PAR_DIRECTEUR);
        folderService.updateFolder(selectedFolder);

        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(director);
        action.setComment("Validation directe par  "+director.getFirstName()+" "+director.getLastName()+": " + newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Alert to officer
        Alert alert = new Alert();
        alert.setTargetUser(officer);
        alert.setMessage("✅ Le directeur " + director.getUsername() + " a validé directement le dossier '" + selectedFolder.getReference() + "'.");
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "✅ Dossier validé directement et marqué comme terminé.";
        selectedFolder = null;
        loadFoldersForDirector();
    }


    public void rejectDirectly() {
        if (selectedFolder == null) return;

        User director = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        selectedFolder.setNavigation(FolderNavigation.REJETE_PAR_DIRECTEUR);
        folderService.updateFolder(selectedFolder);

        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(director);
        action.setComment("Rejet direct par "+director.getFirstName()+" "+director.getLastName()
        +": "+ newComment);
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Alert to officer
        Alert alert = new Alert();
        alert.setTargetUser(officer);
        alert.setMessage("❌ Le directeur " + director.getUsername() + " a rejeté directement le dossier '" + selectedFolder.getReference() + "'.");
        alertService.createAlert(alert);

        newComment = "";
        creationMessage = "❌ Dossier rejeté directement et marqué comme terminé.";
        selectedFolder = null;
        loadFoldersForDirector();
    }
    
    public void assignToAnalyst() {
        User currentUser = userController.getCurrentUser();
        if (selectedFolder == null || currentUser == null) return;

     // Get analyst from current user's agency
        User defaultAnalyst = selectedFolder.getCreatedBy().getAgency().getDefaultAnalyst();
        if (defaultAnalyst == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Aucun analyste assigné à votre agence.", null));
            return;
        }
        

        selectedFolder.setAssignedTo(defaultAnalyst);

        // Save action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(currentUser);
        action.setComment("envoyé à l’analyste ENG: " + defaultAnalyst.getFirstName()+" " + defaultAnalyst.getLastName()
                          + (newComment != null && !newComment.isEmpty() ? " : " + newComment : ""));
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Set status and update
        selectedFolder.setNavigation(FolderNavigation.ASSIGNE_A_ANALYSTE);
        folderService.updateFolder(selectedFolder);

        // Alert to analyst
        Alert alert = new Alert();
        alert.setTargetUser(defaultAnalyst);
        alert.setMessage("📥 Dossier envoyé avec commentaire : " + selectedFolder.getReference());
        alertService.createAlert(alert);

        newComment = "";
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "✅ Dossier envoyé à l'analyste.", null));
    }
    
    public void assignToCICAnalyst() {
    	User currentUser = userController.getCurrentUser();
        if (selectedFolder == null || currentUser == null) return;

     // Get analyst from current user's agency
        User defaultAnalyst = selectedFolder.getCreatedBy().getAgency().getCICAnalyst();
        if (defaultAnalyst == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Aucun analyste assigné à votre agence.", null));
            return;
        }
        
        selectedFolder.setAssignedCIC(defaultAnalyst);

        // Save action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(currentUser);
        action.setComment("envoyé à l’analyste CIC: " + defaultAnalyst.getFirstName()+" " + defaultAnalyst.getLastName()+" :"
                          + (newComment != null && !newComment.isEmpty() ? " : " + newComment : ""));
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Set status and update
        selectedFolder.setNavigation(FolderNavigation.ASSIGNE_A_ANALYSTECIC);
        folderService.updateFolder(selectedFolder);

        // Alert to analyst
        Alert alert = new Alert();
        alert.setTargetUser(defaultAnalyst);
        alert.setMessage("📥 Dossier envoyé avec commentaire : " + selectedFolder.getReference());
        alertService.createAlert(alert);

        newComment = "";
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "✅ Dossier envoyé à l'analyste.", null));
    	
    }
    
    public void assignToAnalystbySuc() {
        User currentUser = userController.getCurrentUser();
        if (selectedFolder == null || currentUser == null) return;

        // Get the agency of the user who originally created the folder
        User folderCreator = selectedFolder.getCreatedBy();
        if (folderCreator == null || folderCreator.getAgency() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Impossible d’identifier l’agence d’origine du dossier.", null));
            return;
        }

        Agency originAgency = folderCreator.getAgency();
        User defaultAnalyst = originAgency.getDefaultAnalyst();

        if (defaultAnalyst == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "❌ Aucun analyste défini pour l’agence d’origine.", null));
            return;
        }

        // Assign folder to analyst from origin agency
        selectedFolder.setAssignedTo(defaultAnalyst);

        // Save folder action
        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(currentUser);
        action.setComment(" envoyé à " + defaultAnalyst.getFirstName()+" "+defaultAnalyst.getLastName()+" : "
                          + (newComment != null && !newComment.isEmpty() ? " : " + newComment : ""));
        action.setActionDate(LocalDate.now());
        folderActionService.saveAction(action);

        // Update navigation
        selectedFolder.setNavigation(FolderNavigation.ASSIGNE_A_ANALYSTE);
        folderService.updateFolder(selectedFolder);

        // Send alert to analyst
        Alert alert = new Alert();
        alert.setTargetUser(defaultAnalyst);
        alert.setMessage("📥 Nouveau dossier reçu: " + selectedFolder.getReference());
        alertService.createAlert(alert);

        newComment = "";
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "✅ Dossier envoyé à l'analyste.", null));
    }



    public void loadFoldersForAnalyst() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            User current = userController.getCurrentUser();
            if (current != null && current.getRole() == Role.Analyste) {
                foldersAssignedToMe = folderService.getFoldersAssignedToAnalyst(current);
                filteredFolders = null;
                selectedFolder = null; // only clear on first load
            }
        }
    }



    public void loadFoldersForResponsableAnalyst() {
        User current = userController.getCurrentUser();
        if (current != null && current.getRole() == Role.RESPONSABLE_ANALYSTE) {
            analystFolders = folderService.getAllFoldersAssignedToAnalysts(); // call your FolderService
            filteredFolders = null;
            selectedFolder = null;
            System.out.println("📦 Loaded all folders for Responsable Analyste");
        }
    }
    
    public void handleResponsableDecision(String decisionType) {
        if (selectedFolder == null) return;

        User responsable = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(responsable);
        action.setActionDate(LocalDate.now());

        Alert alert = new Alert();
        alert.setTargetUser(officer);

        if ("approve".equalsIgnoreCase(decisionType)) {
            selectedFolder.setNavigation(FolderNavigation.APPROUVE_PAR_ENGAGEMENT);
            action.setComment("Approuvé par " +responsable.getFirstName()+" "+responsable.getLastName()+" : "+ newComment);
            alert.setMessage(responsable.getFirstName()+responsable.getLastName()+" a validé le dossier '" + selectedFolder.getReference() + "'.");
            creationMessage = "✅ Dossier approuvé avec succès.";
        } else if ("reject".equalsIgnoreCase(decisionType)) {
            selectedFolder.setNavigation(FolderNavigation.REJETE_PAR_ENGAGEMENT);
            action.setComment("Rejeté par " +responsable.getFirstName()+" "+responsable.getLastName()+" : "+ newComment);
            alert.setMessage(responsable.getFirstName()+responsable.getLastName()+" a rejeté le dossier '" + selectedFolder.getReference() + "'.");
            creationMessage = "❌ Dossier rejeté par le Le Directeur Engagement.";
        } else {
            return; // invalid input
        }

        folderActionService.saveAction(action);
        folderService.updateFolder(selectedFolder);
        alertService.createAlert(alert);

        newComment = "";
        System.out.println("🔔 Le Directeur Engagement a pris une décision : " + decisionType);
    }
    
    public void handleCICDecision(String decisionType) {
        if (selectedFolder == null) return;

        User responsable = userController.getCurrentUser();
        User officer = selectedFolder.getCreatedBy();

        FolderAction action = new FolderAction();
        action.setFolder(selectedFolder);
        action.setUser(responsable);
        action.setActionDate(LocalDate.now());

        Alert alert = new Alert();
        alert.setTargetUser(officer);

        if ("approve".equalsIgnoreCase(decisionType)) {
            selectedFolder.setNavigation(FolderNavigation.APPROUVE_PAR_CIC);
            action.setComment("Approuvé par " +responsable.getFirstName()+" "+responsable.getLastName()+" : "+ newComment);
            alert.setMessage(responsable.getFirstName()+responsable.getLastName()+" a validé le dossier '" + selectedFolder.getReference() + "'.");
            creationMessage = "✅ Dossier approuvé avec succès.";
        } else if ("reject".equalsIgnoreCase(decisionType)) {
            selectedFolder.setNavigation(FolderNavigation.REJETE_PAR_CIC);
            action.setComment("Rejeté par " +responsable.getFirstName()+" "+responsable.getLastName()+" : "+ newComment);
            alert.setMessage(responsable.getFirstName()+responsable.getLastName()+" a rejeté le dossier '" + selectedFolder.getReference() + "'.");
            creationMessage = "❌ Dossier rejeté par le Le Directeur CIC.";
        } else {
            return; // invalid input
        }

        folderActionService.saveAction(action);
        folderService.updateFolder(selectedFolder);
        alertService.createAlert(alert);

        newComment = "";
        System.out.println("🔔 Le Directeur CIC a pris une décision : " + decisionType);
    }





    private transient List<Folder> filteredFolders;

    public List<Folder> getFilteredFolders() {
        return filteredFolders;
    }

    public void setFilteredFolders(List<Folder> filteredFolders) {
        this.filteredFolders = filteredFolders;
    }


    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getCreationMessage() { return creationMessage; }
    public void setCreationMessage(String creationMessage) { this.creationMessage = creationMessage; }

    public List<Folder> getMyFolders() { return myFolders; }
    public void setMyFolders(List<Folder> myFolders) { this.myFolders = myFolders; }

    public Folder getSelectedFolder() { return selectedFolder; }
    public void setSelectedFolder(Folder selectedFolder) { this.selectedFolder = selectedFolder; }

    public String getNewComment() { return newComment; }
    public void setNewComment(String newComment) { this.newComment = newComment; }
    
    public Double getRevenuMensuel() { return revenuMensuel; }
    public void setRevenuMensuel(Double revenuMensuel) { this.revenuMensuel = revenuMensuel; }

    public Double getMontantDemande() { return montantDemande; }
    public void setMontantDemande(Double montantDemande) { this.montantDemande = montantDemande; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public Double getTauxAnnuel() { return tauxAnnuel; }
    public void setTauxAnnuel(Double tauxAnnuel) { this.tauxAnnuel = tauxAnnuel; }
    public User getSelectedAnalyst() {
        return selectedAnalyst;
    }

    public void setSelectedAnalyst(User selectedAnalyst) {
        this.selectedAnalyst = selectedAnalyst;
    }

    public List<Folder> getAnalystFolders() {
        if (analystFolders == null) {
            analystFolders = folderService.getFoldersAssignedToUser(userController.getCurrentUser());
        }
        return analystFolders;
    }
    public List<Folder> getcicFolders() {
        return cicFolders;
    }
    
    
    public List<Folder> getFoldersAssignedToMe() {
        return foldersAssignedToMe;
    }

    public void setFoldersAssignedToMe(List<Folder> foldersAssignedToMe) {
        this.foldersAssignedToMe = foldersAssignedToMe;
    }
    public List<Folder> getFoldersAssignedToCIC() {
        return foldersAssignedToCIC;
    }

    public void setFoldersAssignedToCIC(List<Folder> foldersAssignedToCIC) {
        this.foldersAssignedToCIC = foldersAssignedToCIC;
    }
}
