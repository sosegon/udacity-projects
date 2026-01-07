package com.udacity.course3.reviews;

import com.udacity.course3.reviews.model.Comment;
import com.udacity.course3.reviews.model.Product;
import com.udacity.course3.reviews.model.Review;
import com.udacity.course3.reviews.repository.CommentRepository;
import com.udacity.course3.reviews.repository.ProductRepository;
import com.udacity.course3.reviews.repository.ReviewRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ReviewsApplicationTests {

	@Autowired private ProductRepository productRepository;
	@Autowired private ReviewRepository reviewRepository;
	@Autowired private CommentRepository commentRepository;

	@Before
	public void setUp() {
	}

	@Test
	public void contextLoads() {
		assertNotNull(productRepository);
		assertNotNull(reviewRepository);
		assertNotNull(commentRepository);
	}

	@Test
	public void testFindProductById() {
		Product product = new Product("Product test", "Some description", 49.99f);

		// SQL scripts creates and populates the database
		productRepository.save(product);

		Product actual = productRepository.findById(product.getProductId()).get();

		assertNotNull(actual);
		assertEquals(product.getProductId(), actual.getProductId());
	}

	@Test
	public void testFindReviewsByProduct() throws ParseException {
		// Populate the database with product and reviews.
		Product newProduct = new Product("Phone B1", "Samsung smartphone", 399.99f);
		productRepository.save(newProduct);

		Date date1 = new SimpleDateFormat("yyyy/MM/dd").parse("2019/01/01");
		Review review1 = new Review("This is a nice smartphone 1",
				date1, 5, newProduct);

		Date date2 = new SimpleDateFormat("yyyy/MM/dd").parse("2019/02/01");
		Review review2 = new Review("This is a nice smartphone 2",
				date2, 4, newProduct);

		Date date3 = new SimpleDateFormat("yyyy/MM/dd").parse("2019/03/01");
		Review review3 = new Review("This is a nice smartphone 3",
				date3, 3, newProduct);

		reviewRepository.save(review1);
		reviewRepository.save(review2);
		reviewRepository.save(review3);

		// Query the reviews
		List<Review> reviews = reviewRepository.findReviewsByProduct(newProduct);

		assertEquals(3, reviews.size());
	}

	@Test
	public void testFindCommentsByReview() throws ParseException {
		// Populate the database with product and reviews.
		Product newProduct = new Product("Phone B1", "Samsung smartphone", 399.99f);
		productRepository.save(newProduct);

		Date date1 = new SimpleDateFormat("yyyy/MM/dd").parse("2019/01/01");
		Review review1 = new Review("This is a nice smartphone 1",
				date1, 5, newProduct);

		Date date2 = new SimpleDateFormat("yyyy/MM/dd").parse("2019/02/01");
		Review review2 = new Review("This is a nice smartphone 2",
				date2, 4, newProduct);

		Date date3 = new SimpleDateFormat("yyyy/MM/dd").parse("2019/03/01");
		Review review3 = new Review("This is a nice smartphone 3",
				date3, 3, newProduct);

		reviewRepository.save(review1);
		reviewRepository.save(review2);
		reviewRepository.save(review3);

		Comment comment1 = new Comment("I agree with you", date1, review1);
		Comment comment2 = new Comment("I agree with you", date2, review1);
		Comment comment3 = new Comment("I agree with you", date3, review1);

		commentRepository.save(comment1);
		commentRepository.save(comment2);
		commentRepository.save(comment3);

		// Query the comments
		List<Comment> comments1 = commentRepository.findCommentsByReview(review1);
		List<Comment> comments2 = commentRepository.findCommentsByReview(review2);

		assertEquals(3, comments1.size());
		assertEquals(0, comments2.size());
	}
}