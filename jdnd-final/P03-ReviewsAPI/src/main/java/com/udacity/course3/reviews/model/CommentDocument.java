package com.udacity.course3.reviews.model;

import java.util.Date;

public class CommentDocument {

    private String content;

    private Date dateCreation;

    public CommentDocument(String content, Date dateCreation) {
        this.content = content;
        this.dateCreation = dateCreation;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
}
