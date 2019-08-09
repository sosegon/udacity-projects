package com.udacity.course3.reviews.repository;

import com.udacity.course3.reviews.model.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends CrudRepository<Comment, Integer>, CommentRepositoryCustom {
}
