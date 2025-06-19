package mybank.controller;

import com.mybank.controller.AlertController;
import com.mybank.controller.UserController;
import com.mybank.model.Alert;
import com.mybank.model.User;
import com.mybank.service.AlertService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlertControllerTest {

    @InjectMocks
    private AlertController controller;

    @Mock
    private AlertService alertService;

    @Mock
    private UserController userController;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("testuser");
    }

    @Test
    void testGetMyAlerts_withCurrentUser() {
        List<Alert> dummyAlerts = List.of(new Alert(), new Alert());
        when(userController.getCurrentUser()).thenReturn(user);
        when(alertService.getAllAlertsForUser(user)).thenReturn(dummyAlerts);

        List<Alert> result = controller.getMyAlerts();

        assertEquals(2, result.size());
        verify(alertService).getAllAlertsForUser(user);
    }

    @Test
    void testGetMyAlerts_withoutUser() {
        when(userController.getCurrentUser()).thenReturn(null);

        List<Alert> result = controller.getMyAlerts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAlertCount() {
        when(userController.getCurrentUser()).thenReturn(user);
        when(alertService.getAllAlertsForUser(user)).thenReturn(List.of(new Alert(), new Alert(), new Alert()));

        int count = controller.getAlertCount();

        assertEquals(3, count);
    }

    @Test
    void testMarkAlertsAsRead() {
        when(userController.getCurrentUser()).thenReturn(user);
        List<Alert> unread = List.of(new Alert());
        when(alertService.getUnreadAlertsForUser(user)).thenReturn(unread);

        controller.markAlertsAsRead();

        verify(alertService).markAllAsRead(user);
        verify(alertService).getUnreadAlertsForUser(user);
    }

    @Test
    void testMarkAlertsAsRead_withNullUser() {
        when(userController.getCurrentUser()).thenReturn(null);

        controller.markAlertsAsRead();

        verify(alertService, never()).markAllAsRead(any());
    }
}
