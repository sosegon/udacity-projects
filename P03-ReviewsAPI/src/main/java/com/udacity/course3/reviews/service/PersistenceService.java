package com.udacity.course3.reviews.service;

import com.udacity.course3.reviews.model.CommentDocument;
import com.udacity.course3.reviews.model.ReviewDocument;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface PersistenceService {
    public abstract ResponseEntity<ReviewDocument> createReview(int productId, Map<String, String> review);
    public abstract ResponseEntity<List<ReviewDocument>> getReviews(int productId);
    public abstract ResponseEntity<CommentDocument> createCommentForReview(int reviewId, Map<String, String> comment);
    public abstract List<CommentDocument> findCommentsByReviewId(int reviewId);
}
