package com.mybank.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "folder_actions")
public class FolderAction {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "folder_action_seq")
	@SequenceGenerator(name = "folder_action_seq", sequenceName = "FOLDER_ACTION_SEQ", allocationSize = 1)
	private Long id;


    @ManyToOne(optional = false)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", insertable = true, updatable = true)
    private User user;


    @Column(name = "user_comment", nullable = false, length = 1000)
    private String comment;


    @Column(name = "action_date", nullable = false)
    private LocalDate actionDate;

    public FolderAction() {
        this.actionDate = LocalDate.now();
    }

    public FolderAction(Folder folder, User user, String comment) {
        this.folder = folder;
        this.user = user;
        this.comment = comment;
        this.actionDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() { return id; }

    public Folder getFolder() { return folder; }
    public void setFolder(Folder folder) { this.folder = folder; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDate getActionDate() { return actionDate; }
    public void setActionDate(LocalDate actionDate) { this.actionDate = actionDate; }
}
