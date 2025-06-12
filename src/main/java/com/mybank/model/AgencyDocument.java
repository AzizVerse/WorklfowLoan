package com.mybank.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "agency_documents")
public class AgencyDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Lob
    @Column(name = "file_data", nullable = false)
    private byte[] fileData;

    @Column(name = "upload_date", nullable = false)
    private LocalDate uploadDate;

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    private Folder folder;

    public AgencyDocument() {
        this.uploadDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    public LocalDate getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDate uploadDate) { this.uploadDate = uploadDate; }

    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }
}
