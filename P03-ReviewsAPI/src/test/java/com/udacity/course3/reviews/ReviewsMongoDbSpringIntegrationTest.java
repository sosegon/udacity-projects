package com.udacity.course3.reviews;

import com.udacity.course3.reviews.model.CommentDocument;
import com.udacity.course3.reviews.model.ReviewDocument;
import com.udacity.course3.reviews.repository.ReviewMongoRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReviewsMongoDbSpringIntegrationTest {

    @Autowired
    private ReviewMongoRepository reviewMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    public void testEmbeddedDb() {
        final int reviewId = 1;
        ReviewDocument review = new ReviewDocument("New review", new Date(), 5, 1);
        review.setId(reviewId);

        mongoTemplate.save(review, "reviews");

        Assert.assertFalse(mongoTemplate.findAll(ReviewDocument.class, "reviews").isEmpty());
        Assert.assertNotNull(mongoTemplate.findById(reviewId, ReviewDocument.class));
        Assert.assertEquals(reviewId, mongoTemplate.findById(reviewId, ReviewDocument.class).getId());
    }

    @Test
    public void testFindReviewById() {
        final int reviewId = 1;
        ReviewDocument review = new ReviewDocument("New review", new Date(), 5, 1);
        review.setId(reviewId);

        reviewMongoRepository.save(review);

        ReviewDocument expected = mongoTemplate.findById(reviewId, ReviewDocument.class);
        Optional<ReviewDocument> actualReview = reviewMongoRepository.findById(reviewId);

        Assert.assertEquals(expected.getId(), actualReview.get().getId());
    }

    @Test
    public void testFindReviewsByProductId() {
        final int productId = 1;
        ReviewDocument review1 = new ReviewDocument("New review", new Date(), 5, productId);
        review1.setId(0);
        ReviewDocument review2 = new ReviewDocument("New review", new Date(), 5, productId);
        review2.setId(1);

        reviewMongoRepository.save(review1);
        reviewMongoRepository.save(review2);

        List<ReviewDocument> templateReviews = mongoTemplate.findAll(ReviewDocument.class);
        List<ReviewDocument> expectedReviews = new ArrayList<ReviewDocument>();
        templateReviews.forEach(reviewDocument -> {
            if(reviewDocument.getProductId() == productId){
                expectedReviews.add(reviewDocument);
            }
        });

        List<ReviewDocument> actualReviews = reviewMongoRepository.findReviewsByProductId(productId);

        Assert.assertEquals(expectedReviews.size(), actualReviews.size());
    }

    @Test
    public void testFindCommentsByReviewId() {
        final int productId = 1;
        final int reviewId = 1;
        ReviewDocument review = new ReviewDocument("New review", new Date(), 5, productId);
        review.setId(reviewId);
        List<CommentDocument> comments = new ArrayList<CommentDocument>();
        comments.add(new CommentDocument("new comment 1", new Date()));
        comments.add(new CommentDocument("new comment 2", new Date()));
        review.setComments(comments);

        reviewMongoRepository.save(review);

        ReviewDocument expectedReview = mongoTemplate.findById(productId, ReviewDocument.class);

        Optional<ReviewDocument> actualReview = reviewMongoRepository.findById(reviewId);

        Assert.assertEquals(expectedReview.getComments().size(), actualReview.get().getComments().size());
    }
}
