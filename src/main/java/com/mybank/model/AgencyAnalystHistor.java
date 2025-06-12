package com.mybank.model;

import jakarta.persistence.*;


@Entity
@Table(name = "agency_analyst_history")
public class AgencyAnalystHistor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @ManyToOne
    @JoinColumn(name = "old_analyst_id")
    private User oldAnalyst;

    @ManyToOne
    @JoinColumn(name = "new_analyst_id", nullable = false)
    private User newAnalyst;


    // Getters and Setters

    public Long getId() { return id; }

    public Agency getAgency() { return agency; }
    public void setAgency(Agency agency) { this.agency = agency; }

    public User getOldAnalyst() { return oldAnalyst; }
    public void setOldAnalyst(User oldAnalyst) { this.oldAnalyst = oldAnalyst; }

    public User getNewAnalyst() { return newAnalyst; }
    public void setNewAnalyst(User newAnalyst) { this.newAnalyst = newAnalyst; }


} 

// Extend Agency.java with defaultAnalyst field

