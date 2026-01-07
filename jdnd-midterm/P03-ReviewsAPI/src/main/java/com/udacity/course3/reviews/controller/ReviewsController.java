package com.udacity.course3.reviews.controller;

import com.udacity.course3.reviews.model.Product;
import com.udacity.course3.reviews.model.Review;
import com.udacity.course3.reviews.repository.ProductRepository;
import com.udacity.course3.reviews.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Spring REST controller for working with review entity.
 */
@RestController
public class ReviewsController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * Creates a review for a product.
     *
     * @param productId The id of the product.
     * @return The created review or 404 if product id is not found.
     */
    @RequestMapping(value = "/reviews/products/{productId}", method = RequestMethod.POST)
    public ResponseEntity<Review> createReviewForProduct(@PathVariable("productId") Integer productId,
                                                         @RequestBody Map<String, String> review) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<Review>(HttpStatus.NOT_FOUND);
        }

        String content = review.get("content");
        int rating = Integer.parseInt(review.get("rating"));
        Date date = new Date();
        Product product = opProduct.get();

        Review nReview = new Review(content, date, rating, product);
        reviewRepository.save(nReview);

        return new ResponseEntity<Review>(nReview, HttpStatus.OK);
    }

    /**
     * Lists reviews by product.
     *
     * @param productId The id of the product.
     * @return The list of reviews.
     */
    @RequestMapping(value = "/reviews/products/{productId}", method = RequestMethod.GET)
    public ResponseEntity<List<Review>> listReviewsForProduct(@PathVariable("productId") Integer productId) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<List<Review>>(HttpStatus.NOT_FOUND);
        }

        List<Review> reviews = reviewRepository.findReviewsByProduct(opProduct.get());

        if(reviews.isEmpty()) {
            return new ResponseEntity<List<Review>>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<List<Review>>(reviews, HttpStatus.OK);
    }
}