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
    @Test
    void testCountFoldersFromOtherAgencies() {
        User analyst = new User();
        Agency agency = new Agency();
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("analyst", analyst)).thenReturn(countQuery);
        when(countQuery.setParameter("agency", agency)).thenReturn(countQuery);
        when(countQuery.setParameter("termine", FolderNavigation.TERMINE)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(3L);

        long result = folderService.countFoldersFromOtherAgencies(analyst, agency);
        assertEquals(3L, result);
    }
    @Test
    void testGetAllFoldersAssignedToAnalysts() {
        when(em.createQuery(anyString(), eq(Folder.class))).thenReturn(folderQuery);
        when(folderQuery.setParameter("role", Role.Analyste)).thenReturn(folderQuery);
        when(folderQuery.setParameter("dep", Department.ENG)).thenReturn(folderQuery);
        when(folderQuery.setParameter("termine", FolderNavigation.TERMINE)).thenReturn(folderQuery);
        when(folderQuery.getResultList()).thenReturn(List.of(new Folder()));

        List<Folder> result = folderService.getAllFoldersAssignedToAnalysts();
        assertEquals(1, result.size());
    }
    @Test
    void testGetAllFoldersAssignedToCICAnalysts() {
        when(em.createQuery(anyString(), eq(Folder.class))).thenReturn(folderQuery);
        when(folderQuery.setParameter("role", Role.Analyste_CIC)).thenReturn(folderQuery);
        when(folderQuery.setParameter("dep", Department.CIC)).thenReturn(folderQuery);
        when(folderQuery.setParameter("termine", FolderNavigation.TERMINE)).thenReturn(folderQuery);
        when(folderQuery.getResultList()).thenReturn(List.of(new Folder()));

        List<Folder> result = folderService.getAllFoldersAssignedToCICAnalysts();
        assertEquals(1, result.size());
    }
    @Test
    void testGetAllFolders() {
        when(em.createQuery(anyString(), eq(Folder.class))).thenReturn(folderQuery);
        when(folderQuery.getResultList()).thenReturn(List.of(new Folder()));

        List<Folder> result = folderService.getAllFolders();
        assertEquals(1, result.size());
    }
    @Test
    void testAssignFolderToAnalyst() {
        Folder folder = new Folder();
        User analyst = new User();
        folderService.assignFolderToAnalyst(folder, analyst);
        assertEquals(analyst, folder.getAssignedTo());
        verify(em).merge(folder);
    }
    @Test
    void testGetFoldersAssignedToAnalyst() {
        User analyst = new User();

        when(em.createQuery(anyString(), eq(Folder.class))).thenReturn(folderQuery);
        when(folderQuery.setParameter("analyst", analyst)).thenReturn(folderQuery);
        when(folderQuery.setParameter("termine", FolderNavigation.TERMINE)).thenReturn(folderQuery);
        when(folderQuery.getResultList()).thenReturn(List.of(new Folder()));

        List<Folder> result = folderService.getFoldersAssignedToAnalyst(analyst);
        assertEquals(1, result.size());
    }

}
