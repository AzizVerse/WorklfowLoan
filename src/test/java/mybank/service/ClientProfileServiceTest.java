package mybank.service;

import com.mybank.model.ClientProfile;
import com.mybank.model.Folder;
import com.mybank.service.ClientProfileService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClientProfileServiceTest {

    @InjectMocks
    private ClientProfileService clientProfileService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ClientProfile> query;

    private Folder folder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        folder = new Folder();
        folder.setId(1L);
    }

    @Test
    public void testSaveClientProfile() {
        ClientProfile profile = new ClientProfile();
        profile.setFolder(folder);

        clientProfileService.save(profile);

        verify(em, times(1)).persist(profile);
    }

    @Test
    public void testUpdateClientProfile() {
        ClientProfile profile = new ClientProfile();
        profile.setFolder(folder);

        clientProfileService.update(profile);

        verify(em, times(1)).merge(profile);
        verify(em, times(1)).flush();
    }

    @Test
    public void testFindByFolder() {
        when(em.createQuery(anyString(), eq(ClientProfile.class))).thenReturn(query);
        when(query.setParameter(eq("folder"), eq(folder))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new ClientProfile());

        ClientProfile result = clientProfileService.findByFolder(folder);
        assertNotNull(result);
    }

    @Test
    public void testFindAll() {
        when(em.createQuery(anyString(), eq(ClientProfile.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new ClientProfile(), new ClientProfile()));

        List<ClientProfile> profiles = clientProfileService.findAll();
        assertEquals(2, profiles.size());
    }
}
