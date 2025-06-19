package mybank.controller;

import com.mybank.controller.ClientProfileController;
import com.mybank.controller.FolderController;
import com.mybank.controller.UserController;
import com.mybank.model.*;
import com.mybank.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

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
    public void testMarkAsCompleted() {
        when(userController.getCurrentUser()).thenReturn(adminUser);
        agency.setDirector(analystUser);

        folderController.markAsCompleted();

        assertEquals("\uD83D\uDCC1 Le dossier a été marqué comme terminé.", folderController.getCreationMessage());
        verify(folderService).updateFolder(folder);
        verify(folderActionService).saveAction(any(FolderAction.class));
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
        FolderAction action1 = new FolderAction();
        action1.setId(2L);
        FolderAction action2 = new FolderAction();
        action2.setId(1L);

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
}
