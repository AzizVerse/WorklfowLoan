package com.mybank.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "client_profiles")
public class ClientProfile implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "folder_id", nullable = false, unique = true)
    private Folder folder;

    // === Features not already in Folder or DemandeCredit ===

    @Column(name = "age")
    private Integer age;

    @Column(name = "score_credit")
    private Integer scoreCredit;

    @Column(name = "statut_emploi")
    private String statutEmploi;

    @Column(name = "statut_matrimonial")
    private String statutMatrimonial;

    @Column(name = "statut_propriete")
    private String statutPropriete;

    @Column(name = "paiements_mensuels_dette")
    private Double paiementsMensuelsDette;

    @Column(name = "taux_utilisation_carte_credit")
    private Double tauxUtilisationCarteCredit;

    @Column(name = "nombre_lignes_credit_ouvertes")
    private Integer nombreLignesCreditOuvertes;

    @Column(name = "nombre_demandes_credit")
    private Integer nombreDemandesCredit;

    @Column(name = "historique_faillite")
    private Integer historiqueFaillite;

    @Column(name = "precedents_defauts_pret")
    private Integer precedentsDefautsPret;

    @Column(name = "historique_paiement")
    private Integer historiquePaiement;

    @Column(name = "duree_historique_credit")
    private Integer dureeHistoriqueCredit;

    @Column(name = "actifs_totaux")
    private Double actifsTotaux;

    @Column(name = "passifs_totaux")
    private Double passifsTotaux;

    @Column(name = "valeur_nette")
    private Double valeurNette;

    // === Outputs from models

    @Column(name = "pret_approuve")
    private Boolean pretApprouve;

    @Column(name = "probabilite_approbation") 
    private String probabiliteApprobation;    

    @Column(name = "score_risque")
    private Double scoreRisque;

    // ===== 🛠 Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) {this.id=id;}

    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Integer getScoreCredit() { return scoreCredit; }
    public void setScoreCredit(Integer scoreCredit) { this.scoreCredit = scoreCredit; }

    public String getStatutEmploi() { return statutEmploi; }
    public void setStatutEmploi(String statutEmploi) { this.statutEmploi = statutEmploi; }

    public String getStatutMatrimonial() { return statutMatrimonial; }
    public void setStatutMatrimonial(String statutMatrimonial) { this.statutMatrimonial = statutMatrimonial; }

    public String getStatutPropriete() { return statutPropriete; }
    public void setStatutPropriete(String statutPropriete) { this.statutPropriete = statutPropriete; }

    public Double getPaiementsMensuelsDette() { return paiementsMensuelsDette; }
    public void setPaiementsMensuelsDette(Double paiementsMensuelsDette) { this.paiementsMensuelsDette = paiementsMensuelsDette; }

    public Double getTauxUtilisationCarteCredit() { return tauxUtilisationCarteCredit; }
    public void setTauxUtilisationCarteCredit(Double tauxUtilisationCarteCredit) { this.tauxUtilisationCarteCredit = tauxUtilisationCarteCredit; }

    public Integer getNombreLignesCreditOuvertes() { return nombreLignesCreditOuvertes; }
    public void setNombreLignesCreditOuvertes(Integer nombreLignesCreditOuvertes) { this.nombreLignesCreditOuvertes = nombreLignesCreditOuvertes; }

    public Integer getNombreDemandesCredit() { return nombreDemandesCredit; }
    public void setNombreDemandesCredit(Integer nombreDemandesCredit) { this.nombreDemandesCredit = nombreDemandesCredit; }

    public Integer getHistoriqueFaillite() { return historiqueFaillite; }
    public void setHistoriqueFaillite(Integer historiqueFaillite) { this.historiqueFaillite = historiqueFaillite; }

    public Integer getPrecedentsDefautsPret() { return precedentsDefautsPret; }
    public void setPrecedentsDefautsPret(Integer precedentsDefautsPret) { this.precedentsDefautsPret = precedentsDefautsPret; }

    public Integer getHistoriquePaiement() { return historiquePaiement; }
    public void setHistoriquePaiement(Integer historiquePaiement) { this.historiquePaiement = historiquePaiement; }

    public Integer getDureeHistoriqueCredit() { return dureeHistoriqueCredit; }
    public void setDureeHistoriqueCredit(Integer dureeHistoriqueCredit) { this.dureeHistoriqueCredit = dureeHistoriqueCredit; }

    public Double getActifsTotaux() { return actifsTotaux; }
    public void setActifsTotaux(Double actifsTotaux) { this.actifsTotaux = actifsTotaux; }

    public Double getPassifsTotaux() { return passifsTotaux; }
    public void setPassifsTotaux(Double passifsTotaux) { this.passifsTotaux = passifsTotaux; }

    public Double getValeurNette() { return valeurNette; }
    public void setValeurNette(Double valeurNette) { this.valeurNette = valeurNette; }

    public Boolean getPretApprouve() { return pretApprouve; }
    public void setPretApprouve(Boolean pretApprouve) { this.pretApprouve = pretApprouve; }

    public String getProbabiliteApprobation() {
        return probabiliteApprobation;
    }

    public void setProbabiliteApprobation(String probabiliteApprobation) {
        this.probabiliteApprobation = probabiliteApprobation;
    }


    public Double getScoreRisque() { return scoreRisque; }
    public void setScoreRisque(Double scoreRisque) { this.scoreRisque = scoreRisque; }
}
