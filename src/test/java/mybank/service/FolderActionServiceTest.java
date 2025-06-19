package mybank.service;

import com.mybank.model.Folder;
import com.mybank.model.FolderAction;
import com.mybank.service.FolderActionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class FolderActionServiceTest {

    @InjectMocks
    private FolderActionService folderActionService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<FolderAction> typedQuery;

    private Folder mockFolder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockFolder = new Folder();
        mockFolder.setId(1L);
    }

    @Test
    public void testSaveAction() {
        FolderAction action = new FolderAction();
        folderActionService.saveAction(action);
        verify(em, times(1)).persist(action);
    }

    @Test
    public void testGetActionsByFolder() {
        when(em.find(Folder.class, mockFolder.getId())).thenReturn(mockFolder);
        when(em.createQuery(anyString(), eq(FolderAction.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("folder"), eq(mockFolder))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new FolderAction(), new FolderAction()));

        List<FolderAction> actions = folderActionService.getActionsByFolder(mockFolder);
        assertEquals(2, actions.size());
    }
}
