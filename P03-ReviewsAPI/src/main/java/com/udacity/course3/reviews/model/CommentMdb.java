package com.udacity.course3.reviews.model;

import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.Date;

@Document("comments")
public class CommentMdb {

    @Id
    private int id;

    private String content;

    private Date dateCreation;

    private int reviewId;

    public CommentMdb(String content, Date dateCreation, int reviewId) {
        this.content = content;
        this.dateCreation = dateCreation;
        this.reviewId = reviewId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }
}
