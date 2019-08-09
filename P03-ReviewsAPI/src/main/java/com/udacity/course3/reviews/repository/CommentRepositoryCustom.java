package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Comment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.List;

@NoRepositoryBean
public interface CommentRepositoryCustom {

    @Query(
            value = "SELECT * FROM comments c " +
                    "WHERE c.review_id = :review_id",
            nativeQuery = true)
    List<Comment> findCommentsByReviewId(@Param("review_id") int reviewId);
}
