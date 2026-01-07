package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Comment;
import com.udacity.course3.reviews.model.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Integer> {
    List<Comment> findCommentsByReview(Review review);
}
