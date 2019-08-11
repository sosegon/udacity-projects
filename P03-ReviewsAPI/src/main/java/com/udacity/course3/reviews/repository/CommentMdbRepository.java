package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.CommentMdb;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMdbRepository extends MongoRepository<CommentMdb, Integer> {
    List<CommentMdb> findByReviewId(int reviewId);
}
