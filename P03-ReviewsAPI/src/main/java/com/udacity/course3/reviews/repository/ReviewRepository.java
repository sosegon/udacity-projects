package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Review;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends CrudRepository<Review, Integer>, ReviewRepositoryCustom {
}
