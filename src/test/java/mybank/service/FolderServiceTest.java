package mybank.service;

import com.mybank.model.*;
import com.mybank.service.FolderService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FolderServiceTest {

    @InjectMocks
    private FolderService folderService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Folder> folderQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @Mock
    private TypedQuery<String> agencyNameQuery;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateFolder() {
        Folder folder = new Folder();
        folderService.createFolder(folder);
        verify(em).persist(folder);
    }

    @Test
    void testFindByReference() {
        String ref = "REF123";
        Folder folder = new Folder();
        when(em.createQuery(anyString(), eq(Folder.class))).thenReturn(folderQuery);
        when(folderQuery.setParameter("ref", ref)).thenReturn(folderQuery);
        when(folderQuery.getSingleResult()).thenReturn(folder);

        Folder result = folderService.findByReference(ref);
        assertNotNull(result);
        verify(folderQuery).setParameter("ref", ref);
    }

    @Test
    void testGetFoldersByUser() {
        User user = new User();
        user.setId(1L);

        when(em.createQuery(anyString(), eq(Folder.class))).thenReturn(folderQuery);
        when(folderQuery.setParameter("userId", 1L)).thenReturn(folderQuery);
        when(folderQuery.getResultList()).thenReturn(Collections.singletonList(new Folder()));

        List<Folder> folders = folderService.getFoldersByUser(user);
        assertEquals(1, folders.size());
    }

    @Test
    void testUpdateFolder() {
        Folder folder = new Folder();
        folder.setReference("REF-01");
        folderService.updateFolder(folder);
        verify(em).merge(folder);
    }

    @Test
    void testGetFolderCountForAnalyst() {
        User analyst = new User();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(5L);

        long count = folderService.getFolderCountForAnalyst(analyst);
        assertEquals(5L, count);
    }

    @Test
    void testGetOriginAgencyNamesForCIC() {
        User analyst = new User();
        Agency agency = new Agency();
        when(em.createQuery(anyString(), eq(String.class))).thenReturn(agencyNameQuery);
        when(agencyNameQuery.setParameter("user", analyst)).thenReturn(agencyNameQuery);
        when(agencyNameQuery.setParameter("agency", agency)).thenReturn(agencyNameQuery);
        when(agencyNameQuery.getResultList()).thenReturn(List.of("Tunis Agency"));

        List<String> names = folderService.getOriginAgencyNamesForCIC(analyst, agency);
        assertEquals(1, names.size());
        assertEquals("Tunis Agency", names.get(0));
    }
}
