package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Comment;

import java.util.List;

public interface CommentRepositoryCustom {
    List<Comment> findCommentsByReviewId(int reviewId);
}
