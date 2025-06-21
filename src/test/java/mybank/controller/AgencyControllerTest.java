package mybank.controller;

import com.mybank.controller.AgencyController;
import com.mybank.model.Agency;
import com.mybank.model.User;
import com.mybank.service.AgencyService;
import com.mybank.service.FolderService;
import com.mybank.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AgencyControllerTest {

    private AgencyController agencyController;

    @Mock
    private AgencyService agencyService;

    @Mock
    private UserService userService;

    @Mock
    private FolderService folderService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // ✅ Create instance manually since no constructor is defined
        agencyController = new AgencyController();

        // ✅ Inject mocks using setter methods
        agencyController.setAgencyService(agencyService);
        agencyController.setUserService(userService);
        agencyController.setFolderService(folderService);

        agencyController.init();
    }

    @Test
    public void testCreateAgency_Success() {
        User director = new User();
        director.setId(1L);

        agencyController.setCode("AG123");
        agencyController.setName("Test Agency");
        agencyController.setDirectorId(1L);

        when(userService.findById(1L)).thenReturn(director);
        when(agencyService.findAllAgencies()).thenReturn(List.of());

        String result = agencyController.createAgency();

        verify(agencyService).createAgency(any(Agency.class));
        assertEquals("Agence créée avec succès.", agencyController.getCreationMessage());
        assertNull(result);
    }

    @Test
    public void testCreateAgency_DirectorNotFound() {
        agencyController.setDirectorId(99L);
        when(userService.findById(99L)).thenReturn(null);

        String result = agencyController.createAgency();

        assertEquals("Directeur introuvable.", agencyController.getCreationMessage());
        assertNull(result);
        verify(agencyService, never()).createAgency(any());
    }

    @Test
    public void testGetAvailableDirectors_FilterAssigned() {
        User director1 = new User(); director1.setId(1L);
        User director2 = new User(); director2.setId(2L);

        Agency assignedAgency = new Agency(); assignedAgency.setDirector(director1);
        when(userService.findUsersByRole("DIRECTEUR_AGENCE")).thenReturn(List.of(director1, director2));
        when(agencyService.findAllAgencies()).thenReturn(List.of(assignedAgency));

        agencyController.init();

        List<User> result = agencyController.getAvailableDirectors();
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }
}
