package com.mybank.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Column(name = "description")
    private String description;
    
    @Column(name = "client")
    private String client;

    @Enumerated(EnumType.STRING)
    @Column(name = "navigation", nullable = false)
    private FolderNavigation navigation;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "last_updated")
    private LocalDate lastUpdated;

    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @ManyToOne
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;
    
    @ManyToOne
    @JoinColumn(name = "assigned_Suc_id")
    private User assignedSuc;
    
    @ManyToOne
    @JoinColumn(name = "assigned_CIC_id")
    private User assignedCIC;
    
 // === 🚀 Nouveaux champs pour la simulation de crédit ===
    @Column(name = "revenu_mensuel")
    private Double revenuMensuel;

    @Column(name = "montant_demande")
    private Double montantDemande;

    @Column(name = "duree_mois")
    private Integer dureeMois;

    @Column(name = "taux_annuel")
    private Double tauxAnnuel;

    // List of comments/actions by users on this folder
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FolderAction> actions;
    
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AgencyDocument> agencyDocuments;
    
    @OneToOne(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    private DemandeCredit demandeCredit;



    public Folder() {
        this.createdDate = LocalDate.now();
        this.lastUpdated = LocalDate.now();
        this.navigation = FolderNavigation.CREE_PAR_CHARGE;
    }

    @PreUpdate
    public void onUpdate() {
        this.lastUpdated = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public FolderNavigation getNavigation() { return navigation; }
    public void setNavigation(FolderNavigation navigation) { this.navigation = navigation; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDate lastUpdated) { this.lastUpdated = lastUpdated; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public List<FolderAction> getActions() { return actions; }
    public void setActions(List<FolderAction> actions) { this.actions = actions; }

	public String getClient() {return client;}
    public void setClient(String client) {this.client=client;}
    
    public Double getRevenuMensuel() { return revenuMensuel; }
    public void setRevenuMensuel(Double revenuMensuel) { this.revenuMensuel = revenuMensuel; }

    public Double getMontantDemande() { return montantDemande; }
    public void setMontantDemande(Double montantDemande) { this.montantDemande = montantDemande; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public Double getTauxAnnuel() { return tauxAnnuel; }
    public void setTauxAnnuel(Double tauxAnnuel) { this.tauxAnnuel = tauxAnnuel; }
    
    public List<AgencyDocument> getAgencyDocuments() {
        return agencyDocuments;
    }

    public void setAgencyDocuments(List<AgencyDocument> agencyDocuments) {
        this.agencyDocuments = agencyDocuments;
    }
    
    public DemandeCredit getDemandeCredit() {
        return demandeCredit;
    }

    public void setDemandeCredit(DemandeCredit demandeCredit) {
        this.demandeCredit = demandeCredit;
    }
    public User getAssignedSuc() {
        return assignedSuc;
    }

    public void setAssignedSuc(User assignedSuc) {
        this.assignedSuc = assignedSuc;
    }
    
    public User getAssignedCIC() {
        return assignedCIC;
    }

    public void setAssignedCIC(User assignedCIC) {
        this.assignedCIC = assignedCIC;
    }
    
    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Folder)) return false;
        Folder that = (Folder) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
    public String getFormattedDate() {
        return createdDate != null ? createdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
    }



}
