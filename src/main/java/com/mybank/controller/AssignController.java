package com.mybank.controller;

import com.mybank.model.Agency;
import com.mybank.model.User;
import com.mybank.service.AgencyService;
import com.mybank.service.UserService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
/**
 * JSF managed bean - must use field injection due to JSF lifecycle requirements.
 * Constructor injection causes deployment issues (e.g. WELD-001410).
 */
@Named("assignController")
@SessionScoped
@SuppressWarnings("squid:S6813") // Sonar false positive: field injection is required in JSF
public class AssignController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private UserService userService;

    @Inject
    private AgencyService agencyService;

    private Long selectedUserId;
    private Long selectedAgencyId;

    public void assignUserToAgency() {
        User user = userService.findById(selectedUserId);
        Agency agency = agencyService.findById(selectedAgencyId);

        if (user != null && agency != null) {
            userService.assignUserToAgency(user, agency);
        }
    }

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public List<Agency> getAllAgencies() {
        return agencyService.findAllAgencies();
    }

    // Getters and Setters
    public Long getSelectedUserId() { return selectedUserId; }
    public void setSelectedUserId(Long selectedUserId) { this.selectedUserId = selectedUserId; }

    public Long getSelectedAgencyId() { return selectedAgencyId; }
    public void setSelectedAgencyId(Long selectedAgencyId) { this.selectedAgencyId = selectedAgencyId; }
}
