package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Review;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewRepositoryCustomImpl implements ReviewRepositoryCustom {

    @Autowired
    ReviewRepository reviewRepository;

    @Override
    public List<Review> findReviewsByProductId(int productId) {
        Iterable<Review> itReview = reviewRepository.findAll();
        List<Review> reviews = new ArrayList<Review>();

        itReview.forEach(review -> {
            if(review.getProduct().getProductId() == productId) {
                reviews.add(review);
            }
        });

        return reviews;
    }

    @Override
    public List<Review> findReviewsByProductIdRatingAtLeast(int productId, int rating) {
        Iterable<Review> itReview = reviewRepository.findAll();
        List<Review> reviews = new ArrayList<Review>();

        itReview.forEach(review -> {
            if(review.getProduct().getProductId() == productId &&
                review.getRating() >= rating) {
                reviews.add(review);
            }
        });

        return reviews;
    }

    @Override
    public List<Review> findReviewsByProductIdSinceDate(int productId, Date date) {
        Iterable<Review> itReview = reviewRepository.findAll();
        List<Review> reviews = new ArrayList<Review>();

        itReview.forEach(review -> {
            if(review.getProduct().getProductId() == productId &&
                    review.getDateCreation().compareTo(date) >= 0) {
                reviews.add(review);
            }
        });

        return reviews;
    }
}
