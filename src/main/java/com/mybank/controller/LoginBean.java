package com.mybank.controller;

import com.mybank.model.User;
import com.mybank.service.UserService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    private User currentUser;

    @Inject
    private UserService userService;

    public String login() {
        // You can add authentication logic here
        this.currentUser = userService.getUserByUsername(username);

        if (currentUser != null) {
            return "myFolders.xhtml?faces-redirect=true";
        } else {
            return "login.xhtml"; // or show an error message
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void logout() {
        currentUser = null;
        username = "";
        password = "";
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
}
