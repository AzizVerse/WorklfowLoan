package com.mybank.model;


import jakarta.persistence.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricule", unique = true, nullable = false)
    private String matricule;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @ManyToOne
    @JoinColumn(name = "agency_id")
    private Agency agency;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private Department department;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "niveau")
    private Niveau niveau;

    public User() {}

    public User(String matricule, String firstName, String lastName, String username, String password, Role role , Department department, Niveau niveau) {
        this.matricule = matricule;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = hashPassword(password);
        this.role = role;
        this.department = department;
        this.niveau = niveau;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getMatricule() { return matricule; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    
    public void setId(Long id) { this.id = id; }
    public void setMatricule(String matricule) { this.matricule = matricule; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = hashPassword(password); }
    public void setRole(Role role) { this.role = role; }
    public void setAgency(Agency agency) {
        this.agency = agency;
    }

    public Agency getAgency() {
        return agency;
    }
    
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
    
    public Niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(Niveau niveau) {
        this.niveau = niveau;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }


}
