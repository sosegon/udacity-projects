package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    @Autowired
    CommentRepository commentRepository;

    @Override
    public List<Comment> findCommentsByReviewId(int reviewId) {
        Iterable<Comment> itComment = commentRepository.findAll();
        List<Comment> comments = new ArrayList<Comment>();

        itComment.forEach(comment -> {
            if(comment.getReview().getReviewId() == reviewId) {
                comments.add(comment);
            }
        });

        return comments;
    }
}
