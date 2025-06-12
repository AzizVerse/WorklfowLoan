package com.mybank.controller;

import java.io.Serializable;
import java.util.List;

import com.mybank.model.Alert;
import com.mybank.model.User;
import com.mybank.service.AlertService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("alertController")
@SessionScoped
public class AlertController implements Serializable {

    @Inject
    private AlertService alertService;

    @Inject
    private UserController userController;
    
    private List<Alert> alertsForCurrentUser;


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
            alertsForCurrentUser = alertService.getUnreadAlertsForUser(currentUser); // refresh
        }
    }


}
