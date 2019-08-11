package com.udacity.course3.reviews.service;

import com.udacity.course3.reviews.model.CommentMdb;
import com.udacity.course3.reviews.model.Product;
import com.udacity.course3.reviews.model.ReviewMdb;
import com.udacity.course3.reviews.repository.CommentMdbRepository;
import com.udacity.course3.reviews.repository.ProductRepository;
import com.udacity.course3.reviews.repository.ReviewMdbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersistenceServiceImpl implements PersistenceService {

    @Autowired
    private CommentMdbRepository commentMdbRepository;

    @Autowired
    private ReviewMdbRepository reviewMdbRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ResponseEntity<ReviewMdb> createReview(int productId, Map<String, String> review) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<ReviewMdb>(HttpStatus.NOT_FOUND);
        }

        String content = review.get("content");
        int rating = Integer.parseInt(review.get("rating"));
        Date date = new Date();

        ReviewMdb nReview = new ReviewMdb(content, date, rating, productId);

        reviewMdbRepository.save(nReview);

        return new ResponseEntity<ReviewMdb>(nReview, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<ReviewMdb>> getReviews(int productId) {
        Optional<Product> opProduct = productRepository.findById(productId);

        if(!opProduct.isPresent()) {
            return new ResponseEntity<List<ReviewMdb>>(HttpStatus.NOT_FOUND);
        }

        List<ReviewMdb> reviews = reviewMdbRepository.findByProductId(productId);

        if(reviews.isEmpty()) {
            return new ResponseEntity<List<ReviewMdb>>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<List<ReviewMdb>>(reviews, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CommentMdb> createCommentForReview(int reviewId, Map<String, String> comment) {

        Optional<ReviewMdb> review = reviewMdbRepository.findById(reviewId);

        if(!review.isPresent()) {
            return new ResponseEntity<CommentMdb>(HttpStatus.NOT_FOUND);
        }

        String content = comment.get("content");
        Date date = new Date();

        CommentMdb nComment = new CommentMdb(content, date, reviewId);
        commentMdbRepository.save(nComment);

        return new ResponseEntity<CommentMdb>(nComment, HttpStatus.OK);
    }

    @Override
    public List<CommentMdb> findCommentsByReviewId(int reviewId) {
        Optional<ReviewMdb> review = reviewMdbRepository.findById(reviewId);

        if(!review.isPresent()) {
            return new ArrayList<CommentMdb>();
        }

        return commentMdbRepository.findByReviewId(reviewId);
    }
}
