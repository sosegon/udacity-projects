package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.ReviewDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewMongoRepository extends MongoRepository<ReviewDocument, Integer> {
    List<ReviewDocument> findByProductId(int productId);
}
