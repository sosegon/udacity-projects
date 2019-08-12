package com.udacity.course3.reviews.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document("reviews")
public class ReviewDocument {

    @Id
    private int id;

    private String content;

    private Date dateCreation;

    private int rating;

    @Indexed
    private int productId;

    private List<CommentMdb> comments = new ArrayList<CommentMdb>();

    public ReviewDocument(String content, Date dateCreation, int rating, int productId) {
        this.content = content;
        this.dateCreation = dateCreation;
        this.rating = rating;
        this.productId = productId;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public List<CommentMdb> getComments() {
        return comments;
    }

    public void setComments(List<CommentMdb> comments) {
        this.comments = comments;
    }
}
