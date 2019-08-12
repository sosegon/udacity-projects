package com.udacity.course3.reviews.service;

import com.udacity.course3.reviews.model.CommentDocument;
import com.udacity.course3.reviews.model.Product;
import com.udacity.course3.reviews.model.ReviewDocument;
import com.udacity.course3.reviews.repository.ProductRepository;
import com.udacity.course3.reviews.repository.ReviewMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersistenceServiceImpl implements PersistenceService {

    @Autowired
    private ReviewMongoRepository reviewMongoRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ResponseEntity<ReviewDocument> createReview(int productId, Map<String, String> review) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<ReviewDocument>(HttpStatus.NOT_FOUND);
        }

        String content = review.get("content");
        int rating = Integer.parseInt(review.get("rating"));
        Date date = new Date();

        ReviewDocument nReview = new ReviewDocument(content, date, rating, productId);

        reviewMongoRepository.save(nReview);

        return new ResponseEntity<ReviewDocument>(nReview, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ReviewDocument>> getReviews(int productId) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<List<ReviewDocument>>(HttpStatus.NOT_FOUND);
        }

        List<ReviewDocument> reviews = reviewMongoRepository.findByProductId(productId);

        if(reviews.isEmpty()) {
            return new ResponseEntity<List<ReviewDocument>>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<List<ReviewDocument>>(reviews, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CommentDocument> createCommentForReview(int reviewId, Map<String, String> comment) {

        Optional<ReviewDocument> review = reviewMongoRepository.findById(reviewId);

        if(!review.isPresent()) {
            return new ResponseEntity<CommentDocument>(HttpStatus.NOT_FOUND);
        }

        String content = comment.get("content");
        Date date = new Date();

        CommentDocument nComment = new CommentDocument(content, date);

        review.get().getComments().add(nComment);
        reviewMongoRepository.deleteById(reviewId);
        reviewMongoRepository.save(review.get());

        return new ResponseEntity<CommentDocument>(nComment, HttpStatus.OK);
    }

    @Override
    public List<CommentDocument> findCommentsByReviewId(int reviewId) {
        Optional<ReviewDocument> review = reviewMongoRepository.findById(reviewId);

        if(!review.isPresent()) {
            return new ArrayList<CommentDocument>();
        }

        return reviewMongoRepository.findById(reviewId).get().getComments();
    }
}
