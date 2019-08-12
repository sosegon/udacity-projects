package com.udacity.course3.reviews.controller;

import com.udacity.course3.reviews.model.CommentDocument;
import com.udacity.course3.reviews.service.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Spring REST controller for working with comment entity.
 */
@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Autowired
    private PersistenceService persistenceService;

    /**
     * Creates a comment for a review.
     *
     * @param reviewId The id of the review.
     */
    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.POST)
    public ResponseEntity<CommentDocument> createCommentForReview(@PathVariable("reviewId") Integer reviewId,
                                                                  @RequestBody Map<String, String> comment) {

        return persistenceService.createCommentForReview(reviewId, comment);
    }

    /**
     * List comments for a review.
     *
     * @param reviewId The id of the review.
     */
    @RequestMapping(value = "/reviews/{reviewId}", method = RequestMethod.GET)
    public List<CommentDocument> listCommentsForReview(@PathVariable("reviewId") Integer reviewId) {

        return persistenceService.findCommentsByReviewId(reviewId);
    }
}