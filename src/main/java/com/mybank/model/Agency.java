package com.mybank.model;


import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "agency")
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    // One-to-One relationship with the director (User with role AGENCY_DIRECTOR)
    @OneToOne
    @JoinColumn(name = "director_id", unique = true)
    private User director;
    
    @ManyToOne
    @JoinColumn(name = "default_analyst_id")
    private User defaultAnalyst;
    
    @ManyToOne
    @JoinColumn(name = "CICanalyst_id")
    private User CICAnalyst;
    
    @ManyToOne
    @JoinColumn(name = "responsable_succursale_id")
    private User responsableSuccursale;

    // One-to-Many relationship with users
    @OneToMany(mappedBy = "agency", cascade = CascadeType.ALL)
    private List<User> users;

    public Agency() {}

    public Agency(String code, String name, User director) {
        this.code = code;
        this.name = name;
        this.director = director;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getDirector() { return director; }
    public void setDirector(User director) { this.director = director; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
    
 // Getters and Setters
    public User getDefaultAnalyst() {
        return defaultAnalyst;
    }
    public void setDefaultAnalyst(User defaultAnalyst) {
        this.defaultAnalyst = defaultAnalyst;
    }
    
    public User getCICAnalyst() {
        return CICAnalyst;
    }
    public void setCICAnalyst(User CICAnalyst) {
        this.CICAnalyst = CICAnalyst;
    }
 // Getter
    public User getResponsableSuccursale() {
        return responsableSuccursale;
    }

    // Setter
    public void setResponsableSuccursale(User responsableSuccursale) {
        this.responsableSuccursale = responsableSuccursale;
    }
 
}
