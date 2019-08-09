package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Review;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

@NoRepositoryBean
public interface ReviewRepositoryCustom {
    @Query(
            value = "SELECT * FROM reviews r " +
                    "WHERE r.product_id = :product_id",
            nativeQuery = true)
    List<Review> findReviewsByProductId(@Param("product_id") int productId);

    @Query(
            value = "SELECT * FROM reviews r " +
                    "WHERE r.product_id = :product_id AND r.rating >= :rating",
            nativeQuery = true)
    List<Review> findReviewsByProductIdRatingAtLeast(@Param("product_id") int productId, @Param("rating") int rating);

    @Query(
            value = "SELECT * FROM reviews r " +
                    "WHERE r.product_id = :product_id AND r.date_creation >= :date",
            nativeQuery = true)
    List<Review> findReviewsByProductIdSinceDate(@Param("product_id") int productId, @Param("date") Date date);
}
