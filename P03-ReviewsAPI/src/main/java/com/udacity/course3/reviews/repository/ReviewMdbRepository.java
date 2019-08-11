package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.ReviewMdb;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMdbRepository extends MongoRepository<ReviewMdb, Integer> {
    List<ReviewMdb> findByProductId(int productId);
}
