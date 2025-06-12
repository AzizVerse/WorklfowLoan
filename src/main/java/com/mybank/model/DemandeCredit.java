package com.mybank.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "demandes_credit")
public class DemandeCredit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "folder_id", nullable = false, unique = true)
    private Folder folder;

    @Column(name = "mensualite_estimee")
    private Double mensualiteEstimee;

    @Column(name = "taux_endettement")
    private Double tauxEndettement;
    
    @Column(name = "taux_endettement_avant")
    private Double tauxEndettementAvant; 


    public Long getId() {
        return id;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Double getMensualiteEstimee() {
        return mensualiteEstimee;
    }

    public void setMensualiteEstimee(Double mensualiteEstimee) {
        this.mensualiteEstimee = mensualiteEstimee;
    }

    public Double getTauxEndettement() {
        return tauxEndettement;
    }

    public void setTauxEndettement(Double tauxEndettement) {
        this.tauxEndettement = tauxEndettement;
    }
    

    public Double getTauxEndettementAvant() { return tauxEndettementAvant; }
    public void setTauxEndettementAvant(Double tauxEndettementAvant) { this.tauxEndettementAvant = tauxEndettementAvant; }

    
}
