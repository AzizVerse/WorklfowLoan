package mybank.controller;

import com.mybank.controller.UserController;
import com.mybank.model.*;
import com.mybank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private UserController userController;

    @Mock
    private UserService userService;

    private User mockUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        userController = new UserController();
        userController.setUserService(userService);

        mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("testpass");
        mockUser.setRole(Role.CHARGE_DOSSIER);
        mockUser.setDepartment(Department.ENG);
        mockUser.setFirstName("Test");
        mockUser.setLastName("User");
    }

    @Test
    public void testLogin_SuccessfulOfficerRedirect() {
        userController.setUsername("testuser");
        userController.setPassword("testpass");

        when(userService.authenticate("testuser", "testpass")).thenReturn("CHARGE_DOSSIER");
        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);

        String result = userController.login();

        assertTrue(userController.isLoggedIn());
        assertEquals("officer_dashboard.xhtml?faces-redirect=true", result);
        assertNull(userController.getLoginError());
    }

    @Test
    public void testLogin_Failure() {
        userController.setUsername("invalid");
        userController.setPassword("wrong");

        when(userService.authenticate("invalid", "wrong")).thenReturn(null);

        String result = userController.login();

        assertFalse(userController.isLoggedIn());
        assertEquals("Identifiants incorrects. Veuillez réessayer.", userController.getLoginError());
        assertNull(result);
    }

    @Test
    public void testRegister_DuplicateUsername() {
        userController.setUsername("testuser");

        when(userService.getUserByUsername("testuser")).thenReturn(mockUser);

        String result = userController.register();

        assertEquals("Nom d'utilisateur déjà utilisé.", userController.getRegistrationMessage());
        assertNull(result);
    }

    @Test
    public void testRegister_Success() {
        userController.setUsername("newuser");
        userController.setPassword("newpass");
        userController.setMatricule("12345");
        userController.setFirstName("John");
        userController.setLastName("Doe");
        userController.setRole(Role.ADMIN);
        userController.setDepartment(Department.AGENCY);
        userController.setNiveau(Niveau.N2);

        when(userService.getUserByUsername("newuser")).thenReturn(null);

        String result = userController.register();

        assertEquals("Utilisateur enregistré avec succès.", userController.getRegistrationMessage());
        assertEquals("login.xhtml?faces-redirect=true", result);
        verify(userService).createUser(any(User.class));
    }
}
