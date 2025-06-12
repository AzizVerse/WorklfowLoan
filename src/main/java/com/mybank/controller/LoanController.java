package com.mybank.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.primefaces.model.file.UploadedFile;
import org.primefaces.event.FileUploadEvent;

@Named
@ViewScoped
public class LoanController implements Serializable {

    private List<Folder> folders;
    private Folder selectedFolder;

    private LoanRequest newLoan = new LoanRequest();
    private List<UploadedFile> uploadedFiles = new ArrayList<>();

    @PostConstruct
    public void init() {
        folders = new ArrayList<>();
        folders.add(new Folder("REF001", "Ali Ben Salah", new Date()));
        folders.add(new Folder("REF002", "Salma Trabelsi", new Date()));
    }

    public void prepareNewLoan() {
        newLoan = new LoanRequest();
        uploadedFiles.clear(); // Clear uploaded files for a fresh loan
    }

    public void submitLoanRequest() {
        if (selectedFolder != null) {
            // Attach uploaded files to the new loan
            newLoan.setAttachedFiles(new ArrayList<>(uploadedFiles));

            selectedFolder.getLoanRequests().add(newLoan);
            System.out.println("New loan added to folder: " + selectedFolder.getRef());

            // Show confirmation message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Loan request added successfully for folder: " + selectedFolder.getRef(), ""));
        }
    }

    // Handle file upload
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        uploadedFiles.add(file);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "File uploaded successfully: " + file.getFileName(), ""));
    }

    // Getters & Setters

    public List<Folder> getFolders() {
        return folders;
    }

    public Folder getSelectedFolder() {
        return selectedFolder;
    }

    public void setSelectedFolder(Folder selectedFolder) {
        this.selectedFolder = selectedFolder;
    }

    public LoanRequest getNewLoan() {
        return newLoan;
    }

    public void setNewLoan(LoanRequest newLoan) {
        this.newLoan = newLoan;
    }

    public List<UploadedFile> getUploadedFiles() {
        return uploadedFiles;
    }

    // Inner classes for Folder and LoanRequest

    public static class Folder {
        private String ref;
        private String clientName;
        private Date date;
        private List<LoanRequest> loanRequests = new ArrayList<>();

        public Folder(String ref, String clientName, Date date) {
            this.ref = ref;
            this.clientName = clientName;
            this.date = date;
        }

        // Getters and Setters
        public String getRef() {
            return ref;
        }

        public String getClientName() {
            return clientName;
        }

        public Date getDate() {
            return date;
        }

        public List<LoanRequest> getLoanRequests() {
            return loanRequests;
        }
    }

    public static class LoanRequest {
        private BigDecimal amount;
        private String purpose;
        private int termMonths;
        private List<UploadedFile> attachedFiles = new ArrayList<>();

        // Getters and Setters
        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public int getTermMonths() {
            return termMonths;
        }

        public void setTermMonths(int termMonths) {
            this.termMonths = termMonths;
        }

        public List<UploadedFile> getAttachedFiles() {
            return attachedFiles;
        }

        public void setAttachedFiles(List<UploadedFile> attachedFiles) {
            this.attachedFiles = attachedFiles;
        }
    }
}
