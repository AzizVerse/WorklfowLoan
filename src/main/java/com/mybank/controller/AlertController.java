package com.mybank.controller;

import java.io.Serializable;
import java.util.List;

import com.mybank.model.Alert;
import com.mybank.model.User;
import com.mybank.service.AlertService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
/**
 * JSF managed bean - must use field injection due to JSF lifecycle requirements.
 * Constructor injection causes deployment issues (e.g. WELD-001410).
 */
@Named("alertController")
@SessionScoped
@SuppressWarnings("squid:S6813") // Sonar false positive: field injection is required in JSF
public class AlertController implements Serializable {

    @Inject
    private AlertService alertService;

    @Inject
    private UserController userController;

    public List<Alert> getMyAlerts() {
        User currentUser = userController.getCurrentUser();
        return (currentUser != null) ? alertService.getAllAlertsForUser(currentUser) : List.of();
    }

    public int getAlertCount() {
        return getMyAlerts().size();
    }

    public void markAlertsAsRead() {
        User currentUser = userController.getCurrentUser();
        if (currentUser != null) {
            alertService.markAllAsRead(currentUser);
        }
    }
}
