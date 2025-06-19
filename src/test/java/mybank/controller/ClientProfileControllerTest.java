package mybank.controller;

import com.mybank.controller.ClientProfileController;
import com.mybank.controller.FolderController;
import com.mybank.model.ClientProfile;
import com.mybank.model.Folder;
import com.mybank.service.ClientProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientProfileControllerTest {

    @InjectMocks
    private ClientProfileController controller;

    @Mock
    private FolderController folderBean;

    @Mock
    private ClientProfileService clientProfileService;

    private Folder folder;

    @BeforeEach
    void setUp() {
        folder = new Folder();
    }

    @Test
    void testLoadProfile_existingProfile() {
        ClientProfile existing = new ClientProfile();
        when(folderBean.getSelectedFolder()).thenReturn(folder);
        when(clientProfileService.findByFolder(folder)).thenReturn(existing);

        controller.loadProfile();

        assertEquals(existing, controller.getClientProfile());
    }

    @Test
    void testLoadProfile_newProfile() {
        when(folderBean.getSelectedFolder()).thenReturn(folder);
        when(clientProfileService.findByFolder(folder)).thenReturn(null);

        controller.loadProfile();

        ClientProfile result = controller.getClientProfile();
        assertNotNull(result);
        assertEquals(folder, result.getFolder());
    }

    @Test
    void testSaveProfile_insert() {
        ClientProfile newProfile = new ClientProfile();
        newProfile.setFolder(folder);
        controller.setClientProfile(newProfile);

        when(clientProfileService.findByFolder(folder)).thenReturn(null);

        controller.saveProfile();

        verify(clientProfileService).save(newProfile);
        verify(folderBean).reloadSelectedFolder();
    }

    @Test
    void testSaveProfile_update() {
        ClientProfile existing = new ClientProfile();
        existing.setId(42L);
        ClientProfile toUpdate = new ClientProfile();
        toUpdate.setFolder(folder);
        controller.setClientProfile(toUpdate);

        when(clientProfileService.findByFolder(folder)).thenReturn(existing);

        controller.saveProfile();

        assertEquals(42L, controller.getClientProfile().getId());
        verify(clientProfileService).update(toUpdate);
        verify(folderBean).reloadSelectedFolder();
    }

    @Test
    void testUpdateValeurNette_valid() {
        ClientProfile cp = new ClientProfile();
        cp.setActifsTotaux(2000.0);
        cp.setPassifsTotaux(800.0);
        controller.setClientProfile(cp);

        controller.updateValeurNette();

        assertEquals(1200.0, cp.getValeurNette());
    }

    @Test
    void testUpdateValeurNette_nulls() {
        ClientProfile cp = new ClientProfile();
        cp.setActifsTotaux(null);
        cp.setPassifsTotaux(100.0);
        controller.setClientProfile(cp);

        controller.updateValeurNette();

        assertNull(cp.getValeurNette());
    }
}
