package com.udacity.course3.reviews.controller;

import com.udacity.course3.reviews.model.Comment;
import com.udacity.course3.reviews.model.CommentDocument;
import com.udacity.course3.reviews.model.Review;
import com.udacity.course3.reviews.model.ReviewDocument;
import com.udacity.course3.reviews.repository.CommentRepository;
import com.udacity.course3.reviews.repository.ReviewMongoRepository;
import com.udacity.course3.reviews.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

/**
 * Spring REST controller for working with comment entity.
 */
@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewMongoRepository reviewMongoRepository;

    /**
     * Creates a comment for a review.
     *
     * @param reviewId The id of the review.
     */
    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.POST)
    public ResponseEntity<CommentDocument> createCommentForReview(@PathVariable("reviewId") Integer reviewId,
                                                                  @RequestBody @Valid Comment comment) {

        Optional<Review> opReview = reviewRepository.findById(reviewId);
        Optional<ReviewDocument> opReviewDocument = reviewMongoRepository.findById(reviewId);

        if(!opReview.isPresent() && !opReviewDocument.isPresent()) {
            return new ResponseEntity<CommentDocument>(HttpStatus.NOT_FOUND);
        } else if(!opReview.isPresent()) {
            System.out.println("Error: Review present in Mongodb but not in MySql.");
            return new ResponseEntity<CommentDocument>(HttpStatus.NOT_FOUND);
        } else if(!opReviewDocument.isPresent()) {
            System.out.println("Error: Review present in MySql but not in Mongodb.");
            return new ResponseEntity<CommentDocument>(HttpStatus.NOT_FOUND);
        }

        Date date = new Date();
        Review review = opReview.get();

        comment.setDateCreation(date);
        comment.setReview(review);

        // Persist to MySql
        commentRepository.save(comment);

        CommentDocument nCommentDocument = new CommentDocument(comment.getContent(), date);

        // Persist to Mongodb
        opReviewDocument.get().getComments().add(nCommentDocument);
        reviewMongoRepository.save(opReviewDocument.get());

        return new ResponseEntity<CommentDocument>(nCommentDocument, HttpStatus.OK);
    }

    /**
     * List comments for a review.
     *
     * @param reviewId The id of the review.
     */
    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.GET)
    public List<CommentDocument> listCommentsForReview(@PathVariable("reviewId") Integer reviewId) {

        Optional<Review> opReview = reviewRepository.findById(reviewId);
        Optional<ReviewDocument> opReviewDocument = reviewMongoRepository.findById(reviewId);

        if(!opReview.isPresent() && !opReviewDocument.isPresent()) {
            return new ArrayList<CommentDocument>();
        } else if(!opReview.isPresent()) {
            System.out.println("Error: Review present in Mongodb but not in MySql.");
            return new ArrayList<CommentDocument>();
        } else if(!opReviewDocument.isPresent()) {
            System.out.println("Error: Review present in MySql but not in Mongodb.");
            return new ArrayList<CommentDocument>();
        }

        return reviewMongoRepository.findById(reviewId).get().getComments();
    }
}