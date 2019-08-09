package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Review;

import java.util.Date;
import java.util.List;

public interface ReviewRepositoryCustom {
    List<Review> findReviewsByProductId(int productId);
    List<Review> findReviewsByProductIdRatingAtLeast(int productId, int rating);
    List<Review> findReviewsByProductIdSinceDate(int productId, Date date);
}
