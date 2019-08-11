package com.udacity.course3.reviews.service;

import com.udacity.course3.reviews.model.CommentMdb;
import com.udacity.course3.reviews.model.ReviewMdb;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface PersistenceService {
    public abstract ResponseEntity<ReviewMdb> createReview(int productId, Map<String, String> review);
    public abstract ResponseEntity<List<ReviewMdb>> getReviews(int productId);
    public abstract ResponseEntity<CommentMdb> createCommentForReview(int reviewId, Map<String, String> comment);
    public abstract List<CommentMdb> findCommentsByReviewId(int reviewId);
}
