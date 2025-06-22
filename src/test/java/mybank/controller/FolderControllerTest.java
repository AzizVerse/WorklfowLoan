package mybank.controller;

import com.mybank.controller.ClientProfileController;
import com.mybank.controller.FolderController;
import com.mybank.controller.UserController;
import com.mybank.model.*;
import com.mybank.service.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.faces.context.FacesContext;
import org.mockito.MockedStatic;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FolderControllerTest {

    private FolderController folderController;

    @Mock private FolderService folderService;
    @Mock private UserService userService;
    @Mock private UserController userController;
    @Mock private FolderActionService folderActionService;
    @Mock private AlertService alertService;
    @Mock private AgencyService agencyService;
    @Mock private DemandeCreditService demandeCreditService;
    @Mock private ClientProfileService clientProfileService;
    @Mock private ClientProfileController clientProfileController;

    private Folder folder;
    private User adminUser;
    private User analystUser;
    private Agency agency;
    private static MockedStatic<FacesContext> staticMockedContext;
    private FacesContext mockContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup FacesContext static mock
        staticMockedContext = mockStatic(FacesContext.class);
        mockContext = mock(FacesContext.class);
        staticMockedContext.when(FacesContext::getCurrentInstance).thenReturn(mockContext);
        doNothing().when(mockContext).addMessage(any(), any());

        // Controller under test
        folderController = new FolderController();
        folderController.setFolderService(folderService);
        folderController.setUserService(userService);
        folderController.setUserController(userController);
        folderController.setFolderActionService(folderActionService);
        folderController.setAlertService(alertService);
        folderController.setAgencyService(agencyService);
        folderController.setDemandeCreditService(demandeCreditService);
        folderController.setClientProfileService(clientProfileService);
        folderController.setClientProfileController(clientProfileController);

        // Sample data setup
        folder = new Folder();
        folder.setId(1L);
        folder.setReference("FOLDER-123");
        folder.setCreatedDate(LocalDate.now());

        analystUser = new User();
        analystUser.setId(2L);
        analystUser.setUsername("analyst");
        analystUser.setRole(Role.Analyste);
        analystUser.setFirstName("Alex");
        analystUser.setLastName("Smith");

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        agency = new Agency();
        agency.setDefaultAnalyst(analystUser);
        adminUser.setAgency(agency);

        folder.setCreatedBy(adminUser);
        folderController.setSelectedFolder(folder);
    }


    @AfterEach
    void tearDown() {
        if (staticMockedContext != null) {
            staticMockedContext.close();
            staticMockedContext = null;
        }
    }

    @Test
    public void testAssignFolderAsAdmin() {
        folderController.setSelectedAnalystId(analystUser.getId());
        when(userService.getUserById(analystUser.getId())).thenReturn(analystUser);
        when(userController.getCurrentUser()).thenReturn(adminUser);

        folderController.assignFolderAsAdmin();

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testAssignCICFolderAsAdmin() {
        // Prepare CIC Analyst
        User cicAnalyst = new User();
        cicAnalyst.setId(10L);
        cicAnalyst.setFirstName("CIC");
        cicAnalyst.setRole(Role.Analyste_CIC);

        // Set the selected CIC Analyst ID
        folderController.setSelectedCICAnalystId(cicAnalyst.getId());

        // Mock dependencies
        when(userService.getUserById(cicAnalyst.getId())).thenReturn(cicAnalyst);
        when(userController.getCurrentUser()).thenReturn(adminUser);

        // Execute
        folderController.assignCICFolderAsAdmin();

        // Verify
        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testLoadFoldersForCurrentUser_chargeDossier() {
        // Prepare user with CHARGE_DOSSIER role
        User chargeUser = new User();
        chargeUser.setId(100L);
        chargeUser.setUsername("chargé");
        chargeUser.setRole(Role.CHARGE_DOSSIER);

        // Prepare folders
        Folder f1 = new Folder(); f1.setCreatedDate(LocalDate.of(2023, 1, 1));
        Folder f2 = new Folder(); f2.setCreatedDate(LocalDate.of(2024, 1, 1));
        Folder f3 = new Folder(); f3.setCreatedDate(LocalDate.of(2022, 1, 1));
        
        // Only f1 and f3 should remain (not TERMINE)
        f1.setNavigation(FolderNavigation.ENVOYE_AU_DIRECTEUR);
        f2.setNavigation(FolderNavigation.TERMINE);
        f3.setNavigation(FolderNavigation.APPROUVE_PAR_DIRECTEUR);

        when(userController.getCurrentUser()).thenReturn(chargeUser);
        when(folderService.getFoldersByUser(chargeUser)).thenReturn(List.of(f1, f2, f3));

        // Execute
        folderController.loadFoldersForCurrentUser();

        // Validate
        List<Folder> result = folderController.getMyFolders();
        assertEquals(2, result.size());
        assertTrue(result.contains(f1));
        assertTrue(result.contains(f3));
        assertFalse(result.contains(f2));

        // Ensure sorting: f1 (2023) before f3 (2022)
        assertEquals(f1, result.get(0));
        assertEquals(f3, result.get(1));
    }
    @Test
    public void testSendToResponsableSuccursale_success() {
        when(userController.getCurrentUser()).thenReturn(adminUser);

        User responsable = new User();
        responsable.setId(20L);
        responsable.setFirstName("Res");
        responsable.setLastName("Succ");

        agency.setResponsableSuccursale(responsable);
        folder.setCreatedBy(adminUser);
        adminUser.setAgency(agency);
        folder.setReference("FOLDER-456");
        folderController.setNewComment("Urgent");

        folderController.sendToResponsableSuccursale();

        assertEquals(FolderNavigation.ENVOYE_AU_SUCCURSALE, folder.getNavigation());
        assertEquals(responsable, folder.getAssignedSuc());

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }


    @Test
    public void testHasResponsableSuccursale_true() {
        User creator = new User();
        creator.setAgency(agency);

        agency.setResponsableSuccursale(analystUser); // Mocked in setup()

        folder.setCreatedBy(creator);

        boolean result = folderController.hasResponsableSuccursale(folder);
        assertTrue(result);
    }

    @Test
    public void testHasResponsableSuccursale_falseWhenNoResponsable() {
        User creator = new User();
        creator.setAgency(agency);

        agency.setResponsableSuccursale(null);
        folder.setCreatedBy(creator);

        boolean result = folderController.hasResponsableSuccursale(folder);
        assertFalse(result);
    }

    @Test
    public void testHasResponsableSuccursale_nullFolder() {
        boolean result = folderController.hasResponsableSuccursale(null);
        assertFalse(result);
    }
    @Test
    public void testIsCurrentUserAssignedSuccursale_true() {
        folder.setAssignedSuc(adminUser); // assign current user as succursale
        when(userController.getCurrentUser()).thenReturn(adminUser);

        folderController.setSelectedFolder(folder);

        boolean result = folderController.isCurrentUserAssignedSuccursale();
        assertTrue(result);
    }

    @Test
    public void testIsCurrentUserAssignedSuccursale_false() {
        folder.setAssignedSuc(analystUser); // different user
        when(userController.getCurrentUser()).thenReturn(adminUser);

        folderController.setSelectedFolder(folder);

        boolean result = folderController.isCurrentUserAssignedSuccursale();
        assertFalse(result);
    }
    @Test
    public void testIsCurrentUserAlsoResponsableSuccursale_true() {
        agency.setResponsableSuccursale(adminUser); // admin is responsible
        adminUser.setAgency(agency);
        folder.setCreatedBy(new User());
        folder.getCreatedBy().setAgency(agency);

        folderController.setSelectedFolder(folder);
        when(userController.getCurrentUser()).thenReturn(adminUser);

        boolean result = folderController.isCurrentUserAlsoResponsableSuccursale();
        assertTrue(result);
    }

    @Test
    public void testIsCurrentUserAlsoResponsableSuccursale_false() {
        agency.setResponsableSuccursale(analystUser); // someone else
        folder.setCreatedBy(new User());
        folder.getCreatedBy().setAgency(agency);

        folderController.setSelectedFolder(folder);
        when(userController.getCurrentUser()).thenReturn(adminUser);

        boolean result = folderController.isCurrentUserAlsoResponsableSuccursale();
        assertFalse(result);
    }


    @Test
    public void testMarkAsCompleted() {
        when(userController.getCurrentUser()).thenReturn(adminUser);
        agency.setDirector(analystUser);

        folderController.markAsCompleted();

        assertEquals("📁 Le dossier a été marqué comme terminé.", folderController.getCreationMessage());
        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testCreateFolderAndProfileAndDemande_success() {
        // Mock current user (chargé)
        when(userController.getCurrentUser()).thenReturn(adminUser);

        // Setup director for alert
        User director = new User();
        director.setId(20L);
        director.setUsername("director");
        agency.setDirector(director);
        adminUser.setAgency(agency);

        // Mock ClientProfile flow
        ClientProfile mockProfile = new ClientProfile();
        when(clientProfileController.getClientProfile()).thenReturn(mockProfile);

        // Set form values
        folderController.setReference("REF-999");
        folderController.setDescription("description");
        folderController.setClient("client-X");
        folderController.setMontantDemande(10000.0);
        folderController.setRevenuMensuel(2000.0);
        folderController.setDureeMois(24);
        folderController.setTauxAnnuel(5.5);

        // Call the method
        String result = folderController.createFolderAndProfileAndDemande();

        // Assertions
        assertEquals("✅ Dossier, Profil Client, et Demande de Crédit créés avec succès.", folderController.getCreationMessage());
        assertEquals("officer_dashboard.xhtml?faces-redirect=true", result);

        // Verify folder was created
        verify(folderService).createFolder(any(Folder.class));
        verify(clientProfileController).saveProfile();
        verify(demandeCreditService).save(any(DemandeCredit.class));
        verify(alertService).createAlert(any(Alert.class));
    }

    @Test
    public void testCreateFolderAndProfileAndDemande_noUser() {
        when(userController.getCurrentUser()).thenReturn(null);

        String result = folderController.createFolderAndProfileAndDemande();

        assertNull(result);
        assertEquals("Erreur : Aucun utilisateur connecté.", folderController.getCreationMessage());
    }
    @Test
    public void testSendToResponsableAnalyste_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Analyse terminée");

        User responsable = new User();
        responsable.setId(11L);
        responsable.setFirstName("Res");
        responsable.setLastName("Analyste");

        when(userController.getCurrentUser()).thenReturn(analystUser);
        when(userService.getGlobalResponsibleAnalyst()).thenReturn(responsable);

        folderController.sendToResponsableAnalyste();

        assertEquals(FolderNavigation.ENVOYE_AU_RESPONSABLE_ANALYSTE, folder.getNavigation());
        assertEquals("✅ Dossier transmis au Responsable Analyste.", folderController.getCreationMessage());

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testSendToDirectorCIC_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Pour CIC");

        User responsable = new User();
        responsable.setId(12L);
        responsable.setFirstName("Directeur");
        responsable.setLastName("CIC");

        when(userController.getCurrentUser()).thenReturn(analystUser);
        when(userService.getGlobalDirectorCIC()).thenReturn(responsable);

        folderController.sendToDirectorCIC();

        assertEquals(FolderNavigation.ENVOYE_AU_DIRECTEUR_CIC, folder.getNavigation());

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testSendToDirectorEngagement_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Besoin de validation");

        User engDirector = new User();
        engDirector.setId(13L);
        engDirector.setFirstName("Directeur");
        engDirector.setLastName("Engagement");

        when(userController.getCurrentUser()).thenReturn(adminUser);
        when(userService.getGlobalDirectorENG()).thenReturn(engDirector);

        folderController.sendToDirectorEngagement();

        assertEquals(FolderNavigation.ENVOYE_AU_DIRECTEUR_ENGAGEMENT, folder.getNavigation());
        assertEquals("✅ Dossier envoyé au Directeur Engagement.", folderController.getCreationMessage());

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testReturnOfferToDirector_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Retour pour décision");

        when(userController.getCurrentUser()).thenReturn(adminUser);

        User director = new User();
        director.setId(99L);
        director.setFirstName("Dir");
        director.setLastName("AG");
        agency.setDirector(director);
        folder.setCreatedBy(adminUser);
        adminUser.setAgency(agency);
        folder.setReference("FOLDER-XYZ");

        folderController.returnOfferToDirector();

        assertEquals(FolderNavigation.RETOUR_OFFRE, folder.getNavigation());
        assertEquals("✅ Dossier retourné au Directeur pour offre finale.", folderController.getCreationMessage());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }



    @Test
    public void testReloadSelectedFolder() {
        folderController.setSelectedFolder(folder);
        when(folderService.findById(folder.getId())).thenReturn(folder);

        folderController.reloadSelectedFolder();

        verify(folderService).findById(folder.getId());
    }

    @Test
    public void testGetActionsForSelectedFolder() {
        folderController.setSelectedFolder(folder);
        FolderAction action1 = new FolderAction(); action1.setId(2L);
        FolderAction action2 = new FolderAction(); action2.setId(1L);

        when(folderActionService.getActionsByFolder(folder)).thenReturn(List.of(action1, action2));

        List<FolderAction> actions = folderController.getActionsForSelectedFolder();
        assertEquals(2, actions.size());
        assertEquals(1L, actions.get(0).getId());
    }

    @Test
    public void testSendToDirector() {
        User director = new User();
        director.setId(5L);
        director.setUsername("director");
        Agency agency = new Agency();
        agency.setDirector(director);
        adminUser.setAgency(agency);

        when(userController.getCurrentUser()).thenReturn(adminUser);
        folderController.setNewComment("Sending to director");
        folderController.sendToDirector();

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }

    @Test
    public void testReturnFolderToOfficer() {
        when(userController.getCurrentUser()).thenReturn(analystUser);
        folderController.setNewComment("Needs more docs");
        folderController.returnToOfficer();

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }

    @Test
    public void testRejectFolder() {
        when(userController.getCurrentUser()).thenReturn(analystUser);
        folderController.setNewComment("Invalid profile");
        folderController.rejectFolder();

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }

    @Test
    public void testApproveFolder() {
        when(userController.getCurrentUser()).thenReturn(analystUser);
        folderController.setNewComment("Approved after review");
        folderController.approveFolder();

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    
    @Test
    public void testReturnToChargeFromAnalyst_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Dossier incomplet");

        User analyst = new User();
        analyst.setId(15L);
        analyst.setUsername("analystENG");

        folder.setCreatedBy(adminUser); // officer
        folder.setReference("F-2025-123");

        when(userController.getCurrentUser()).thenReturn(analyst);

        folderController.returnToChargeFromAnalyst();

        assertEquals(FolderNavigation.RETOURNE_DE_Analyste, folder.getNavigation());
        assertEquals("✅ Dossier retourné au chargé d’agence.", folderController.getCreationMessage());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testReturnToChargeFromAnalystCIC_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Dossier à corriger");

        User analystCIC = new User();
        analystCIC.setId(20L);
        analystCIC.setUsername("cic_analyst");

        folder.setCreatedBy(adminUser);
        folder.setReference("CIC-456");

        when(userController.getCurrentUser()).thenReturn(analystCIC);

        folderController.returnToChargeFromAnalystCIC();

        assertEquals(FolderNavigation.RETOURNE_DE_AnalysteCIC, folder.getNavigation());
        assertEquals("✅ Dossier retourné au chargé d’agence.", folderController.getCreationMessage());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testSendToAnalystAgain_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Veuillez revoir les pièces justificatives.");

        User sender = adminUser;
        User analyst = new User();
        analyst.setId(88L);
        analyst.setFirstName("Ana");
        analyst.setLastName("Lyst");
        analyst.setUsername("analyst");

        folder.setAssignedTo(analyst);
        folder.setReference("RE-001");

        when(userController.getCurrentUser()).thenReturn(sender);

        folderController.sendToAnalystAgain();

        assertEquals(FolderNavigation.ASSIGNE_A_ANALYSTE, folder.getNavigation());
        assertEquals("✅ Dossier renvoyé à l’analyste : analyst", folderController.getCreationMessage());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testSendToAnalystCICAgain_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Revoir les justificatifs");

        User currentUser = adminUser;
        User analystCIC = new User();
        analystCIC.setId(99L);
        analystCIC.setFirstName("Cic");
        analystCIC.setLastName("Analyst");
        analystCIC.setUsername("cic_analyst");

        folder.setAssignedCIC(analystCIC);
        folder.setReference("CIC-RE-001");

        when(userController.getCurrentUser()).thenReturn(currentUser);

        folderController.sendToAnalystCICAgain();

        assertEquals(FolderNavigation.ASSIGNE_A_ANALYSTECIC, folder.getNavigation());
        assertEquals("✅ Dossier renvoyé à l’analyste : cic_analyst", folderController.getCreationMessage());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }

    @Test
    public void testValidateDirectly_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Tout est bon");

        User director = adminUser;
        folder.setCreatedBy(analystUser);
        folder.setReference("FD-VAL-001");

        when(userController.getCurrentUser()).thenReturn(director);

        folderController.validateDirectly();

        assertEquals("✅ Dossier validé directement et marqué comme terminé.", folderController.getCreationMessage());
        assertNull(folderController.getSelectedFolder());

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testRejectDirectly_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Dossier non conforme");

        User director = adminUser;
        folder.setCreatedBy(analystUser);
        folder.setReference("FD-REJ-001");

        when(userController.getCurrentUser()).thenReturn(director);

        folderController.rejectDirectly();

        assertEquals("❌ Dossier rejeté directement et marqué comme terminé.", folderController.getCreationMessage());
        assertNull(folderController.getSelectedFolder());

        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testAssignToAnalyst_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Analyse urgente");

        User currentUser = adminUser;
        User analyst = new User();
        analyst.setId(55L);
        analyst.setFirstName("Ali");
        analyst.setLastName("Nalyse");

        Agency agency = new Agency();
        agency.setDefaultAnalyst(analyst);

        User officer = new User();
        officer.setAgency(agency);

        folder.setCreatedBy(officer);
        folder.setReference("ASSIGN-001");

        when(userController.getCurrentUser()).thenReturn(currentUser);

        folderController.assignToAnalyst();

        assertEquals(FolderNavigation.ASSIGNE_A_ANALYSTE, folder.getNavigation());
        assertEquals(analyst, folder.getAssignedTo());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testAssignToCICAnalyst_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Analyse prioritaire CIC");

        User currentUser = adminUser;
        User analystCIC = new User();
        analystCIC.setId(66L);
        analystCIC.setFirstName("Cic");
        analystCIC.setLastName("Analyst");

        Agency agency = new Agency();
        agency.setCICAnalyst(analystCIC);

        User officer = new User();
        officer.setAgency(agency);

        folder.setCreatedBy(officer);
        folder.setReference("ASSIGN-CIC-002");

        when(userController.getCurrentUser()).thenReturn(currentUser);

        folderController.assignToCICAnalyst();

        assertEquals(FolderNavigation.ASSIGNE_A_ANALYSTECIC, folder.getNavigation());
        assertEquals(analystCIC, folder.getAssignedCIC());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testAssignToAnalystBySuc_success() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Analyse depuis la succursale");

        User currentUser = adminUser;
        User analyst = new User();
        analyst.setId(99L);
        analyst.setFirstName("Analyste");
        analyst.setLastName("Succursale");

        Agency originAgency = new Agency();
        originAgency.setDefaultAnalyst(analyst);

        User folderCreator = new User();
        folderCreator.setAgency(originAgency);

        folder.setCreatedBy(folderCreator);
        folder.setReference("SUCC-001");

        when(userController.getCurrentUser()).thenReturn(currentUser);

        folderController.assignToAnalystbySuc();

        assertEquals(analyst, folder.getAssignedTo());
        assertEquals(FolderNavigation.ASSIGNE_A_ANALYSTE, folder.getNavigation());

        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }

    
    @Test
    public void testLoadFoldersForResponsableAnalyst_success() {
        User responsable = new User();
        responsable.setRole(Role.RESPONSABLE_ANALYSTE);

        when(userController.getCurrentUser()).thenReturn(responsable);
        when(folderService.getAllFoldersAssignedToAnalysts()).thenReturn(List.of(folder));

        folderController.loadFoldersForResponsableAnalyst();

        assertEquals(1, folderController.getAnalystFolders().size());
        assertNull(folderController.getFilteredFolders());
        assertNull(folderController.getSelectedFolder());
    }
    @Test
    public void testHandleResponsableDecision_approve() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Analyse complète OK");

        User responsable = new User();
        responsable.setFirstName("Ali");
        responsable.setLastName("Ben Salem");

        User officer = new User();
        officer.setUsername("officer123");
        folder.setCreatedBy(officer);
        folder.setReference("F-123");

        when(userController.getCurrentUser()).thenReturn(responsable);

        folderController.handleResponsableDecision("approve");

        assertEquals(FolderNavigation.APPROUVE_PAR_ENGAGEMENT, folder.getNavigation());
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
        assertEquals("", folderController.getNewComment());
    }

    @Test
    public void testHandleResponsableDecision_reject() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Manque de pièces justificatives");

        User responsable = new User();
        responsable.setFirstName("Amel");
        responsable.setLastName("Mansouri");

        User officer = new User();
        officer.setUsername("officer456");
        folder.setCreatedBy(officer);
        folder.setReference("F-456");

        when(userController.getCurrentUser()).thenReturn(responsable);

        folderController.handleResponsableDecision("reject");

        assertEquals(FolderNavigation.REJETE_PAR_ENGAGEMENT, folder.getNavigation());
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }
    @Test
    public void testHandleCICDecision_approve() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Validé pour CIC");

        User responsable = new User();
        responsable.setFirstName("CIC");
        responsable.setLastName("Manager");

        User officer = new User();
        officer.setUsername("officer789");
        folder.setCreatedBy(officer);
        folder.setReference("F-789");

        when(userController.getCurrentUser()).thenReturn(responsable);

        folderController.handleCICDecision("approve");

        assertEquals(FolderNavigation.APPROUVE_PAR_CIC, folder.getNavigation());
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }

    @Test
    public void testHandleCICDecision_reject() {
        folderController.setSelectedFolder(folder);
        folderController.setNewComment("Non conforme CIC");

        User responsable = new User();
        responsable.setFirstName("CIC");
        responsable.setLastName("Dir");

        User officer = new User();
        officer.setUsername("officer007");
        folder.setCreatedBy(officer);
        folder.setReference("F-007");

        when(userController.getCurrentUser()).thenReturn(responsable);

        folderController.handleCICDecision("reject");

        assertEquals(FolderNavigation.REJETE_PAR_CIC, folder.getNavigation());
        verify(folderActionService).saveAction(any(FolderAction.class));
        verify(folderService).updateFolder(folder);
        verify(alertService).createAlert(any(Alert.class));
    }

}
