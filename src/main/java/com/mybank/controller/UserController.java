	package com.mybank.controller;
	
	import com.mybank.model.*;
import com.mybank.service.AgencyAnalystHistoryService;
import com.mybank.service.UserService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
	import jakarta.faces.context.FacesContext;
	import jakarta.inject.Inject;
	import jakarta.inject.Named;
	import java.io.Serializable;
	import java.util.Arrays;
	import java.util.List;
	
	@Named("userController")
	@SessionScoped
	public class UserController implements Serializable {
	    private static final long serialVersionUID = 1L;
	
	    @Inject
	    private UserService userService;
	    
	    @Inject
	    private AgencyAnalystHistoryService agencyAnalystHistoryService;
	
	    private Long id;
	    private String matricule;
	    private String username;
	    private String password;
	    private boolean loggedIn = false;
	    private String firstName;
	    private String lastName;
	    private Role role;
	    private Department department;
	    private Niveau niveau;
	    private String loginError;
	    private String registrationMessage;
	    private User user;
	    private User responsableAnalyst;
	    private User directeurENG;
	    private User directeurCIC;

	    @PostConstruct
	    public void init() {
	        responsableAnalyst = userService.getGlobalResponsibleAnalyst();
	        directeurENG = userService.getGlobalDirectorENG();
	        directeurCIC = userService.getGlobalDirectorCIC();
	    }
	    
	
	    public String login() {
	        String userRole = userService.authenticate(username, password);
	        if (userRole == null) {
	            loginError = "Identifiants incorrects. Veuillez réessayer.";
	            return null;
	        }
	
	        this.user = userService.getUserByUsername(username);
	        if (user != null) {
	            this.id = user.getId();
	            this.matricule = user.getMatricule();
	            this.firstName = user.getFirstName();
	            this.lastName = user.getLastName();
	            this.role = user.getRole();
	            this.department = user.getDepartment();
	            this.niveau = user.getNiveau();
	            loggedIn = true;
	            loginError = null;
	
	            if (role == Role.CHARGE_DOSSIER) {
	                return "officer_dashboard.xhtml?faces-redirect=true";
	            } else if (role == Role.ADMIN) {
	                return "Admin_dash.xhtml?faces-redirect=true";
	            } else if (role == Role.DIRECTEUR_AGENCE) {
	                return "Director_dashboard.xhtml?faces-redirect=true";
	            } else if (role == Role.Analyste) {
	                return "Analyste_dashboard.xhtml?faces-redirect=true";
	            }else if (role == Role.RESPONSABLE_ANALYSTE) {
	                return "ResponsableENG_dashboard.xhtml?faces-redirect=true";
	            }else if (role == Role.DIRECTEUR_ENGAGEMENT) {
	                return "DirectorENG_dashboard.xhtml?faces-redirect=true";
	            }else if (role == Role.Analyste_CIC) {
	                return "AnalysteCIC_dashboard.xhtml?faces-redirect=true";
	            }else if (role == Role.Directeur_CIC) {
	                return "DirectorCIC_dashboard.xhtml?faces-redirect=true";
	            }else {
	                return "dashboard.xhtml?faces-redirect=true";
	            }
	        }
	
	        return null;
	    }
	
	    public String register() {
	        if (userService.getUserByUsername(username) != null) {
	            registrationMessage = "Nom d'utilisateur déjà utilisé.";
	            return null;
	        }
	
	        User newUser = new User(matricule, firstName, lastName, username, password, role, department, niveau);
	        userService.createUser(newUser);
	        registrationMessage = "Utilisateur enregistré avec succès.";
	        return "login.xhtml?faces-redirect=true";
	    }
	
	    public String logout() {
	        loggedIn = false;
	        id = null;
	        matricule = null;
	        username = null;
	        password = null;
	        firstName = null;
	        lastName = null;
	        role = null;
	        department = null;
	        niveau = null;
	        loginError = null;
	        FacesContext.getCurrentInstance().getExternalContext().invalidateSession(); // <== add this line
	        return "login.xhtml?faces-redirect=true";
	    }
	    
	   
	    
	    

	    
	    public List<User> getAvailableAnalysts() {
	        return userService.getUsersByRoleAndDepartment(Role.Analyste, Department.ENG);
	    }
	    
	    public List<User> getAvailableCICAnalysts() {
	        return userService.getUsersByRoleAndDepartment(Role.Analyste_CIC, Department.CIC);
	    }
	    
	    public User getResponsableAnalyst() {
	        return responsableAnalyst;
	    }
	    

	    public User getDirecteurENG() {
	        return directeurENG;
	    }
	    public User getDirecteurCIC() {
	        return directeurCIC;
	    }

	
	
	    public User getCurrentUser() {
	        return this.user;
	    }
	
	    public Agency getAgency() {
	        return user != null ? user.getAgency() : null;
	    }
	
	    public List<Role> getAvailableRoles() {
	        return Arrays.asList(Role.values());
	    }
	
	    public List<Department> getAvailableDepartments() {
	        return Arrays.asList(Department.values());
	    }
	
	    public List<Niveau> getAvailableNiveaux() {
	        return Arrays.asList(Niveau.values());
	    }
	
	    // Getters and Setters
	    public Long getId() { return id; }
	    public void setId(Long id) { this.id = id; }
	
	    public String getMatricule() { return matricule; }
	    public void setMatricule(String matricule) { this.matricule = matricule; }
	
	    public String getUsername() { return username; }
	    public void setUsername(String username) { this.username = username; }
	
	    public String getPassword() { return password; }
	    public void setPassword(String password) { this.password = password; }
	
	    public boolean isLoggedIn() { return loggedIn; }
	
	    public String getFirstName() { return firstName; }
	    public void setFirstName(String firstName) { this.firstName = firstName; }
	
	    public String getLastName() { return lastName; }
	    public void setLastName(String lastName) { this.lastName = lastName; }
	
	    public Role getRole() { return role; }
	    public void setRole(Role role) { this.role = role; }
	
	    public Department getDepartment() { return department; }
	    public void setDepartment(Department department) { this.department = department; }
	
	    public Niveau getNiveau() { return niveau; }
	    public void setNiveau(Niveau niveau) { this.niveau = niveau; }
	
	    public String getLoginError() { return loginError; }
	    public void setLoginError(String loginError) { this.loginError = loginError; }
	
	    public String getRegistrationMessage() { return registrationMessage; }
	    public void setRegistrationMessage(String registrationMessage) { this.registrationMessage = registrationMessage; }
	}
