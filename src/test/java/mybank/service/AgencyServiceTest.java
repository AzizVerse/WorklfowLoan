package mybank.service;

import com.mybank.model.Agency;
import com.mybank.model.User;
import com.mybank.service.AgencyService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AgencyServiceTest {

    @InjectMocks
    private AgencyService agencyService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Agency> agencyQuery;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAgency() {
        Agency agency = new Agency("AG001", "Tunis Agence", new User());
        agencyService.createAgency(agency);
        verify(em, times(1)).persist(agency);
    }

    @Test
    public void testFindAllAgencies() {
        List<Agency> agencies = List.of(
            new Agency("AG001", "A1", new User()),
            new Agency("AG002", "A2", new User())
        );

        when(em.createQuery("SELECT a FROM Agency a", Agency.class)).thenReturn(agencyQuery);
        when(agencyQuery.getResultList()).thenReturn(agencies);

        List<Agency> result = agencyService.findAllAgencies();

        assertEquals(2, result.size());
        assertEquals("AG001", result.get(0).getCode());
        assertEquals("AG002", result.get(1).getCode());
    }

    @Test
    public void testFindByCode() {
        Agency agency = new Agency("AG123", "TestAgency", new User());

        when(em.createQuery("SELECT a FROM Agency a WHERE a.code = :code", Agency.class)).thenReturn(agencyQuery);
        when(agencyQuery.setParameter("code", "AG123")).thenReturn(agencyQuery);
        when(agencyQuery.getSingleResult()).thenReturn(agency);

        Agency result = agencyService.findByCode("AG123");

        assertNotNull(result);
        assertEquals("TestAgency", result.getName());
    }
}
