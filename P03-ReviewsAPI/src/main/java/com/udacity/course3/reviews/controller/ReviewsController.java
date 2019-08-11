package com.udacity.course3.reviews.controller;

import com.udacity.course3.reviews.model.ReviewMdb;
import com.udacity.course3.reviews.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Spring REST controller for working with review entity.
 */
@RestController
public class ReviewsController {

    @Autowired
    private PersistenceService persistenceService;

    /**
     * Creates a review for a product.
     *
     * @param productId The id of the product.
     * @return The created review or 404 if product id is not found.
     */
    @RequestMapping(value = "/reviews/products/{productId}", method = RequestMethod.POST)
    public ResponseEntity<ReviewMdb> createReviewForProduct(@PathVariable("productId") Integer productId,
                                                         @RequestBody Map<String, String> review) {
        return persistenceService.createReview(productId, review);
    }

    /**
     * Lists reviews by product.
     *
     * @param productId The id of the product.
     * @return The list of reviews.
     */
    @RequestMapping(value = "/reviews/products/{productId}", method = RequestMethod.GET)
    public ResponseEntity<List<ReviewMdb>> listReviewsForProduct(@PathVariable("productId") Integer productId) {
        return persistenceService.getReviews(productId);
    }
}