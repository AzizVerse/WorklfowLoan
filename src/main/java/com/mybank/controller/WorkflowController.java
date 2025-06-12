package com.mybank.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class WorkflowController implements Serializable {

    private List<Folder> folders;
    private Folder selectedFolder;
    private List<Step> steps;
    private List<ActionLog> actions;
    private int selectedStepIndex = 0;

    @PostConstruct
    public void init() {
        folders = new ArrayList<>();
        folders.add(new Folder("REF001", "Ali Ben Salah", new Date()));
        folders.add(new Folder("REF002", "Salma Trabelsi", new Date()));

        // Initialize with the first folder as default
        if (!folders.isEmpty()) {
            selectFolder(folders.get(0));
        }
    }
    public int getSelectedStepIndex() {
        return selectedStepIndex;
    }
    public void setSelectedStepIndex(int selectedStepIndex) {
        this.selectedStepIndex = selectedStepIndex;
    }

    public void selectFolder(Folder folder) {
        this.selectedFolder = folder;

        // Simulate dynamic loading of steps and actions per folder
        steps = List.of(
                new Step("Charger_Agence", "Ahmed", new Date(), "Initial creation"),
                new Step("Directeur_Agence", "Imen", new Date(), "All docs validated"),
                new Step("Analyse_Risk", "Khaled", new Date(), "Credit score OK"),
                new Step("Engagement", "Noura", new Date(), "Approval granted"),
                new Step("Cic", "Samir", new Date(), "Loan disbursed")
        );

        actions = new ArrayList<>();
        actions.add(new ActionLog("Ahmed", "Folder Created", new Date(), "Basic info provided"));
        actions.add(new ActionLog("Imen", "Review Completed", new Date(), "Reviewed documents"));
        actions.add(new ActionLog("Khaled", "Verification Passed", new Date(), "Risk analysis OK"));

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Dossier sélectionné : " + folder.getRef(), ""));
    }

    public void selectStep(int index) {
        selectedStepIndex = index;
    }

    // Getters & Setters

    public List<Folder> getFolders() {
        return folders;
    }

    public Folder getSelectedFolder() {
        return selectedFolder;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public Step getSelectedStep() {
        if (steps != null && !steps.isEmpty()) {
            return steps.get(selectedStepIndex);
        }
        return new Step("Aucune étape sélectionnée", "", null, "");
    }

    public List<ActionLog> getActions() {
        return actions;
    }

    // Inner classes
    public static class Folder {
        private String ref;
        private String clientName;
        private Date date;

        public Folder(String ref, String clientName, Date date) {
            this.ref = ref;
            this.clientName = clientName;
            this.date = date;
        }

        public String getRef() {
            return ref;
        }

        public String getClientName() {
            return clientName;
        }

        public Date getDate() {
            return date;
        }
    }

    public static class Step {
        private String name;
        private String user;
        private Date date;
        private String comments;

        public Step(String name, String user, Date date, String comments) {
            this.name = name;
            this.user = user;
            this.date = date;
            this.comments = comments;
        }

        public String getName() {
            return name;
        }

        public String getUser() {
            return user;
        }

        public Date getDate() {
            return date;
        }

        public String getComments() {
            return comments;
        }
    }

    public static class ActionLog {
        private String user;
        private String actionType;
        private Date date;
        private String notes;

        public ActionLog(String user, String actionType, Date date, String notes) {
            this.user = user;
            this.actionType = actionType;
            this.date = date;
            this.notes = notes;
        }

        public String getUser() {
            return user;
        }

        public String getActionType() {
            return actionType;
        }

        public Date getDate() {
            return date;
        }

        public String getNotes() {
            return notes;
        }
    }
}
