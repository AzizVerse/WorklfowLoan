package mybank.controller;

import com.mybank.controller.DashboardController;
import com.mybank.controller.UserController;
import com.mybank.model.Role;
import com.mybank.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private UserController userController;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
    }

    @Test
    void testLoadDefaultPage_chargeDossier() {
        user.setRole(Role.CHARGE_DOSSIER);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/chargeDemande.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_directeurAgence() {
        user.setRole(Role.DIRECTEUR_AGENCE);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/directFlow.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_analyste() {
        user.setRole(Role.Analyste);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/analysteFlow.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_responsableAnalyste() {
        user.setRole(Role.RESPONSABLE_ANALYSTE);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/ResAnalysteFlow.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_analysteCIC() {
        user.setRole(Role.Analyste_CIC);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/analysteCICFlow.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_directeurEngagement() {
        user.setRole(Role.DIRECTEUR_ENGAGEMENT);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/directENGFlow.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_directeurCIC() {
        user.setRole(Role.Directeur_CIC);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/directCICFlow.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_admin() {
        user.setRole(Role.ADMIN);
        when(userController.getCurrentUser()).thenReturn(user);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/adminFlow.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testLoadDefaultPage_nullUser() {
        when(userController.getCurrentUser()).thenReturn(null);

        dashboardController.loadDefaultPage();
        assertEquals("/pages/login.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testSetPage() {
        dashboardController.setPage("example");
        assertEquals("/pages/example.xhtml", dashboardController.getCurrentPage());
    }

    @Test
    void testGetCurrentPage_triggersDefault() {
        when(userController.getCurrentUser()).thenReturn(null);
        String page = dashboardController.getCurrentPage();
        assertEquals("/pages/login.xhtml", page);
    }
}
