package com.udacity.course3.reviews;

import com.udacity.course3.reviews.model.ReviewMdb;
import com.udacity.course3.reviews.repository.ReviewMdbRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReviewsMongoDbSpringIntegrationTest {

    @Autowired
    private ReviewMdbRepository reviewMdbRepository;

    @DisplayName("given object to save"
            + " when save object using MongoDB template"
            + " then object is saved")
    @Test
    public void testFindByProductId() {
        ReviewMdb review = new ReviewMdb("New review", new Date(), 5, 1);

        ReviewMdb nReview = reviewMdbRepository.save(review);

        Optional<ReviewMdb> opReview = reviewMdbRepository.findById(nReview.getId());

        Assert.assertEquals(nReview.getId(), opReview.get().getId());
    }
}
