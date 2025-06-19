package mybank.service;

import com.mybank.model.Role;
import com.mybank.model.User;
import com.mybank.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<User> query;

    private final String username = "aziz";
    private final String rawPassword = "123456";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(username);
        testUser.setPassword(rawPassword); // will be hashed inside setPassword
        testUser.setRole(Role.Analyste);

        // mocking the query chain
        when(em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class))
                .thenReturn(query);
        when(query.setParameter("username", username)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(testUser);
    }

    @Test
    void testAuthenticateSuccess() {
        String result = userService.authenticate(username, rawPassword);
        assertNotNull(result);
        assertEquals("Analyste", result);
    }

    @Test
    void testAuthenticateFail_WrongPassword() {
        String result = userService.authenticate(username, "wrongpassword");
        assertNull(result);
    }
}
