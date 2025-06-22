package com.mybank.controller;

import com.mybank.model.Role;
import com.mybank.model.User;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
/**
 * JSF managed bean - must use field injection due to JSF lifecycle requirements.
 * Constructor injection causes deployment issues (e.g. WELD-001410).
 */
@Named("dashboardController")
@SessionScoped
@SuppressWarnings("squid:S6813") // Sonar false positive: field injection is required in JSF    
public class DashboardController implements Serializable {
    private static final long serialVersionUID = 1L;

    private String currentPage;

    @Inject
    private UserController userController;

    public String getCurrentPage() {
        if (currentPage == null) {
            loadDefaultPage(); // fallback if not initialized
        }
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = "/pages/" + page + ".xhtml";
    }

    public void loadDefaultPage() {
        User user = userController.getCurrentUser();
        if (user != null) {
            Role role = user.getRole();
            switch (role) {
                case CHARGE_DOSSIER:
                    currentPage = "/pages/chargeDemande.xhtml";
                    break;
                case DIRECTEUR_AGENCE:
                    currentPage = "/pages/directFlow.xhtml";
                    break;
                case Analyste:
                    currentPage = "/pages/analysteFlow.xhtml";
                    break;
                case RESPONSABLE_ANALYSTE:
                    currentPage = "/pages/ResAnalysteFlow.xhtml";
                    break;
                case Analyste_CIC:
                    currentPage = "/pages/analysteCICFlow.xhtml";
                    break;    
                case DIRECTEUR_ENGAGEMENT:
                    currentPage = "/pages/directENGFlow.xhtml";
                    break;
                case Directeur_CIC:
                    currentPage = "/pages/directCICFlow.xhtml";
                    break;    
                case ADMIN:
                    currentPage = "/pages/adminFlow.xhtml";
                    break;
                default:
                    currentPage = "/pages/chargeDemande.xhtml"; // fallback
            }
        } else {
            currentPage = "/pages/login.xhtml"; // if not logged in
        }
    }
}
