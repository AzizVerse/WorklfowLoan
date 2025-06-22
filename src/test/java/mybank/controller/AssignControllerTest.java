package mybank.controller;

import com.mybank.controller.AssignController;
import com.mybank.model.Agency;
import com.mybank.model.User;
import com.mybank.service.AgencyService;
import com.mybank.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class AssignControllerTest {

    private AssignController assignController;
    private UserService userService;
    private AgencyService agencyService;

    @BeforeEach
    public void setup() {
        userService = mock(UserService.class);
        agencyService = mock(AgencyService.class);

        assignController = new AssignController();

        // Inject mocks using setter methods (or use reflection if needed)
        assignController.setSelectedUserId(1L);
        assignController.setSelectedAgencyId(2L);

        // Use reflection to inject the mocks since @Inject is not used in test context
        setField(assignController, "userService", userService);
        setField(assignController, "agencyService", agencyService);
    }

    @Test
    public void testAssignUserToAgency_success() {
        User mockUser = new User();
        Agency mockAgency = new Agency();

        when(userService.findById(1L)).thenReturn(mockUser);
        when(agencyService.findById(2L)).thenReturn(mockAgency);

        assignController.assignUserToAgency();

        verify(userService).assignUserToAgency(mockUser, mockAgency);
    }

    @Test
    public void testAssignUserToAgency_userNotFound() {
        when(userService.findById(1L)).thenReturn(null);
        when(agencyService.findById(2L)).thenReturn(new Agency());

        assignController.assignUserToAgency();

        verify(userService, never()).assignUserToAgency(any(), any());
    }

    @Test
    public void testAssignUserToAgency_agencyNotFound() {
        when(userService.findById(1L)).thenReturn(new User());
        when(agencyService.findById(2L)).thenReturn(null);

        assignController.assignUserToAgency();

        verify(userService, never()).assignUserToAgency(any(), any());
    }

    // Helper for injecting private fields using reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = AssignController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
