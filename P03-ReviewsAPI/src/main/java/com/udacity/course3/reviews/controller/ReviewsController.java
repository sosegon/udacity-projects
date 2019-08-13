package com.udacity.course3.reviews.controller;

import com.udacity.course3.reviews.model.Product;
import com.udacity.course3.reviews.model.Review;
import com.udacity.course3.reviews.model.ReviewDocument;
import com.udacity.course3.reviews.repository.ProductRepository;
import com.udacity.course3.reviews.repository.ReviewMongoRepository;
import com.udacity.course3.reviews.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * Spring REST controller for working with review entity.
 */
@RestController
public class ReviewsController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewMongoRepository reviewMongoRepository;

    /**
     * Creates a review for a product.
     *
     * @param productId The id of the product.
     * @return The created review or 404 if product id is not found.
     */
    @RequestMapping(value = "/reviews/products/{productId}", method = RequestMethod.POST)
    public ResponseEntity<ReviewDocument> createReviewForProduct(@PathVariable("productId") Integer productId,
                                                                 @RequestBody @Valid Review review) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<ReviewDocument>(HttpStatus.NOT_FOUND);
        }

        Date date = new Date();
        Product product = opProduct.get();

        review.setProduct(product);
        review.setDateCreation(date);

        // Persist to MySql. This will generate the id which will be used to
        // persist the document in Mongodb correctly.
        reviewRepository.save(review);

        ReviewDocument nReviewDocument = new ReviewDocument(
                review.getContent(),
                date,
                review.getRating(),
                productId);
        nReviewDocument.setId(review.getReviewId());

        // Persist to Mongodb
        reviewMongoRepository.save(nReviewDocument);

        return new ResponseEntity<ReviewDocument>(nReviewDocument, HttpStatus.OK);
    }

    /**
     * Lists reviews by product.
     *
     * @param productId The id of the product.
     * @return The list of reviews.
     */
    @RequestMapping(value = "/reviews/products/{productId}", method = RequestMethod.GET)
    public ResponseEntity<List<ReviewDocument>> listReviewsForProduct(@PathVariable("productId") Integer productId) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<List<ReviewDocument>>(HttpStatus.NOT_FOUND);
        }

        List<Review> reviews = reviewRepository.findReviewsByProduct(opProduct.get());

        if(reviews.isEmpty()) {
            return new ResponseEntity<List<ReviewDocument>>(HttpStatus.NOT_FOUND);
        }

        // Retrieve from Mongodb. Although the loop below is not good for
        // performance (it would be better to call
        // reviewMongoRepository.findByProductId), it helps to make sure the
        // reviews exists in MySql and Mongodb.
        List<ReviewDocument> mReviews = new ArrayList<ReviewDocument>();
        reviews.forEach(review -> {
            Optional<ReviewDocument> opReview = reviewMongoRepository.findById(review.getReviewId());
            if(opReview.isPresent()) {
                mReviews.add(opReview.get());
            } else {
                System.out.println("Error: Review present in MySql but not in Mongodb.");
            }
        });

        return new ResponseEntity<List<ReviewDocument>>(mReviews, HttpStatus.OK);
    }
}