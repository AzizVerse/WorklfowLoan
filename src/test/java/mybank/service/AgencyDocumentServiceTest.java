package mybank.service;

import com.mybank.model.AgencyDocument;
import com.mybank.model.Folder;
import com.mybank.service.AgencyDocumentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AgencyDocumentServiceTest {

    @InjectMocks
    private AgencyDocumentService agencyDocumentService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<AgencyDocument> query;

    private Folder mockFolder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockFolder = new Folder();
        mockFolder.setId(1L);
    }

    @Test
    public void testSave() {
        AgencyDocument doc = new AgencyDocument();
        agencyDocumentService.save(doc);
        verify(em, times(1)).persist(doc);
    }

    @Test
    public void testFindByFolder() {
        when(em.createQuery(anyString(), eq(AgencyDocument.class))).thenReturn(query);
        when(query.setParameter(eq("folder"), eq(mockFolder))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new AgencyDocument(), new AgencyDocument()));

        List<AgencyDocument> docs = agencyDocumentService.findByFolder(mockFolder);
        assertEquals(2, docs.size());
    }

    @Test
    public void testFindById() {
        AgencyDocument doc = new AgencyDocument();
        doc.setFileName("test.pdf");

        when(em.find(AgencyDocument.class, 1L)).thenReturn(doc);

        AgencyDocument result = agencyDocumentService.findById(1L);
        assertEquals("test.pdf", result.getFileName());
    }

    @Test
    public void testDelete() {
        AgencyDocument doc = new AgencyDocument();

        when(em.contains(doc)).thenReturn(true);
        agencyDocumentService.delete(doc);
        verify(em, times(1)).remove(doc);
    }

    @Test
    public void testDeleteMergeIfDetached() {
        AgencyDocument doc = new AgencyDocument();
        AgencyDocument mergedDoc = new AgencyDocument();

        when(em.contains(doc)).thenReturn(false);
        when(em.merge(doc)).thenReturn(mergedDoc);

        agencyDocumentService.delete(doc);
        verify(em).remove(mergedDoc);
    }
}
