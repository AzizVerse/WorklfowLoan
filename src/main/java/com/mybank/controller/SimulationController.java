package com.mybank.controller;

import com.mybank.model.ClientProfile;
import com.mybank.model.DemandeCredit;
import com.mybank.model.Folder;
import com.mybank.service.ClientProfileService;
import com.mybank.service.DemandeCreditService;
import com.mybank.service.LoanApprovalPredictionService;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("simulationController")
@SessionScoped
public class SimulationController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private FolderController folderBean;

    @Inject
    private DemandeCreditService demandeCreditService;

    @Inject
    private ClientProfileService clientProfileService; // 🆕 Inject ClientProfileService
    
    @Inject
    private LoanApprovalPredictionService loanApprovalPredictionService;
    
    @Inject
    private ClientProfileController clientProfileController;
    
    private DemandeCredit simulatedResult;
    private boolean loading = false;

    public void simulate() {
        Folder folder = folderBean.getSelectedFolder();
        if (folder == null) return;

        loading = true;

        double montant = folder.getMontantDemande();
        int duree = folder.getDureeMois();
        double tauxAnnuel = folder.getTauxAnnuel();
        double revenuMensuel = folder.getRevenuMensuel();

        double tauxMensuel = tauxAnnuel / 12 / 100;
        double mensualite = (montant * tauxMensuel) / (1 - Math.pow(1 + tauxMensuel, -duree));
        double tauxEndettementApres = ((mensualite) / revenuMensuel) * 100;

        // 🧮 Get ClientProfile for tauxEndettementAvant calculation
        ClientProfile profile = clientProfileService.findByFolder(folder);
        double tauxEndettementAvant = 0.0;

        if (profile != null && profile.getPaiementsMensuelsDette() != null && revenuMensuel != 0) {
            tauxEndettementAvant = (profile.getPaiementsMensuelsDette() / revenuMensuel) * 100;
        }

        simulatedResult = new DemandeCredit();
        simulatedResult.setMensualiteEstimee(Math.round(mensualite * 100.0) / 100.0);
        simulatedResult.setTauxEndettement(Math.round(tauxEndettementApres * 100.0) / 100.0);
        simulatedResult.setTauxEndettementAvant(Math.round(tauxEndettementAvant * 100.0) / 100.0); // 🆕
        simulatedResult.setFolder(folder);

        loading = false;
    }
    public void resetSimulation() {
        simulatedResult = null;
    }

    public String getEndettementAdvice() {
        if (simulatedResult == null || simulatedResult.getTauxEndettement() == null) {
            return "";
        }

        double taux = simulatedResult.getTauxEndettement();

        if (taux <= 33) {
            return "🟢 Endettement Sain (≤ 33%) : Situation stable pour un nouveau prêt.";
        } else if (taux <= 40) {
            return "🟡 Endettement Limite (33%-40%) : À analyser avec attention.";
        } else if (taux <= 50) {
            return "🟠 Endettement Élevé (40%-50%) : Risque important, garanties requises.";
        } else {
            return "🔴 Endettement Critique (> 50%) : Demande difficilement acceptable.";
        }
    }
    public String getAdviceForFolderSimulation() {
        DemandeCredit demande = getDemandeCreditBySelectedFolder();
        if (demande == null || demande.getTauxEndettement() == null) {
            return "";
        }
        double taux = demande.getTauxEndettement();

        if (taux <= 33) {
            return "🟢 Endettement Sain (≤ 33%) : Situation stable pour un nouveau prêt.";
        } else if (taux <= 40) {
            return "🟡 Endettement Limite (33%-40%) : À analyser avec attention.";
        } else if (taux <= 50) {
            return "🟠 Endettement Élevé (40%-50%) : Risque important, garanties requises.";
        } else {
            return "🔴 Endettement Critique (> 50%) : Demande difficilement acceptable.";
        }
    }


    public void saveSimulation() {
        Folder selectedFolder = folderBean.getSelectedFolder();
        if (selectedFolder == null || simulatedResult == null) return;

        DemandeCredit existing = demandeCreditService.findByFolder(selectedFolder);

        if (existing == null) {
            DemandeCredit demande = new DemandeCredit();
            demande.setFolder(selectedFolder);
            demande.setMensualiteEstimee(simulatedResult.getMensualiteEstimee());
            demande.setTauxEndettement(simulatedResult.getTauxEndettement());
            demande.setTauxEndettementAvant(simulatedResult.getTauxEndettementAvant()); // 🆕
            demandeCreditService.save(demande);
        } else {
            existing.setMensualiteEstimee(simulatedResult.getMensualiteEstimee());
            existing.setTauxEndettement(simulatedResult.getTauxEndettement());
            existing.setTauxEndettementAvant(simulatedResult.getTauxEndettementAvant()); // 🆕
            demandeCreditService.update(existing);
        }

        simulatedResult = null;
        folderBean.reloadSelectedFolder();
    }

    public DemandeCredit getDemandeCreditBySelectedFolder() {
        Folder folder = folderBean.getSelectedFolder();
        if (folder == null) return null;
        return demandeCreditService.findByFolder(folder);
    }
    
    private boolean loanApproved;
    private Double approvalProbability;
    private Double predictedRiskScore;

    public void checkLoanApproval() {
        Folder folder = folderBean.getSelectedFolder();
        if (folder == null) {
            this.loanApproved = false;
            this.approvalProbability = null;
            this.predictedRiskScore = null;
            return;
        }

        ClientProfile profile = clientProfileService.findByFolder(folder);
        DemandeCredit demande = getDemandeCreditBySelectedFolder();

        if (profile == null || demande == null) {
            this.loanApproved = false;
            this.approvalProbability = null;
            this.predictedRiskScore = null;
            return;
        }

        LoanApprovalPredictionService.PredictionResult result = loanApprovalPredictionService.predictLoanApproval(
            profile.getAge(),
            profile.getScoreCredit(),
            profile.getStatutEmploi(),
            folder.getMontantDemande(),
            folder.getDureeMois(),
            profile.getStatutMatrimonial(),
            profile.getStatutPropriete(),
            folder.getRevenuMensuel(),
            demande.getTauxEndettement()/100,
            profile.getPrecedentsDefautsPret(),
            profile.getHistoriquePaiement(),
            profile.getHistoriqueFaillite(),
            demande.getMensualiteEstimee(),
            profile.getPaiementsMensuelsDette(),
            profile.getValeurNette(),
            folder.getTauxAnnuel()/100
        );

        this.loanApproved = result.isLoanApproved();
        this.approvalProbability = result.getApprovalProbability();
        this.predictedRiskScore = result.getRiskScore();
        
        profile.setPretApprouve(loanApproved);
        profile.setScoreRisque(predictedRiskScore);
        profile.setProbabiliteApprobation(String.format("%.2f", approvalProbability));
        clientProfileService.update(profile);
        clientProfileController.loadProfile();
    }

    public boolean getLoanApproved() {
        return loanApproved;
    }

    public Double getApprovalProbability() {
        return approvalProbability;
    }

    public Double getPredictedRiskScore() {
        return predictedRiskScore;
    }

    

    // Getters & Setters
    public DemandeCredit getSimulatedResult() { return simulatedResult; }
    public void setSimulatedResult(DemandeCredit simulatedResult) { this.simulatedResult = simulatedResult; }
    public boolean isLoading() { return loading; }
    public void setLoading(boolean loading) { this.loading = loading; }
}
