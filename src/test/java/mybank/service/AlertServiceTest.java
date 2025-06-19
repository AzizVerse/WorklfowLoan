package mybank.service;

import com.mybank.model.Alert;
import com.mybank.model.User;
import com.mybank.service.AlertService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AlertServiceTest {

    @InjectMocks
    private AlertService alertService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Alert> typedQuery;

    @Mock
    private Query updateQuery;

    private User mockUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
    }

    @Test
    public void testCreateAlert() {
        Alert alert = new Alert();
        alert.setMessage("New alert");
        alert.setTargetUser(mockUser);

        alertService.createAlert(alert);

        verify(em, times(1)).persist(alert);
    }

    @Test
    public void testGetUnreadAlertsForUser() {
        when(em.createQuery(anyString(), eq(Alert.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), eq(mockUser))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new Alert(), new Alert()));

        List<Alert> results = alertService.getUnreadAlertsForUser(mockUser);

        assertEquals(2, results.size());
    }

    @Test
    public void testMarkAllAsRead() {
        when(em.createQuery(anyString())).thenReturn(updateQuery);
        when(updateQuery.setParameter(anyString(), any())).thenReturn(updateQuery);

        alertService.markAllAsRead(mockUser);

        verify(updateQuery, times(1)).executeUpdate();
    }

    @Test
    public void testGetAllAlertsForUser() {
        when(em.createQuery(anyString(), eq(Alert.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), eq(mockUser))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(new Alert()));

        List<Alert> results = alertService.getAllAlertsForUser(mockUser);

        assertEquals(1, results.size());
    }
}
