package mybank.service;

import com.mybank.model.DemandeCredit;
import com.mybank.model.Folder;
import com.mybank.service.DemandeCreditService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DemandeCreditServiceTest {

    @InjectMocks
    private DemandeCreditService demandeCreditService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<DemandeCredit> query;

    private Folder folder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        folder = new Folder();
        folder.setId(1L);
    }

    @Test
    public void testSave() {
        DemandeCredit dc = new DemandeCredit();
        dc.setFolder(folder);

        demandeCreditService.save(dc);

        verify(em, times(1)).persist(dc);
    }

    @Test
    public void testUpdate() {
        DemandeCredit dc = new DemandeCredit();
        demandeCreditService.update(dc);

        verify(em, times(1)).merge(dc);
    }

    @Test
    public void testFindByFolder() {
        when(em.createQuery(anyString(), eq(DemandeCredit.class))).thenReturn(query);
        when(query.setParameter(eq("folder"), eq(folder))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(new DemandeCredit());

        DemandeCredit result = demandeCreditService.findByFolder(folder);
        assertNotNull(result);
    }

    @Test
    public void testReloadFolder() {
        when(em.find(Folder.class, folder.getId())).thenReturn(folder);

        Folder reloaded = demandeCreditService.reloadFolder(folder);
        assertEquals(folder, reloaded);
    }
}
