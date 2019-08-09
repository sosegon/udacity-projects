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
	public void testFindProductByName() {
		productRepository.save(new Product("Phone B1", "Samsung smartphone", 399.99f));
		productRepository.save(new Product("phone B2", "Huawei smartphone", 259.99f));
		productRepository.save(new Product("phone b3", "Apple smartphone", 799.99f));
		productRepository.save(new Product("fone b3", "Xiaomi smartphone", 299.99f));

		List<Product> products = productRepository.findProductsByName("phone");

		assertEquals(3, products.size());
	}

	@Test
	public void testFindProductsCheaperThan(){
		// SQL scripts creates and populates the database

		List<Product> products = productRepository.findProductsCheaperThan(200);

		assertEquals(2, products.size());
	}

	@Test
	public void testFindProductsMoreExpensiveThan(){
		// SQL scripts creates and populates the database

		List<Product> products = productRepository.findProductsMoreExpensiveThan(200);

		assertEquals(1, products.size());
	}

	@Test
	public void testFindProductsBetweenPrices(){
		// SQL scripts creates and populates the database

		List<Product> products = productRepository.findProductsBetweenPrices(100, 600);

		assertEquals(2, products.size());
	}

	@Test
	public void testFindProductsByNameCheaperThan(){
		productRepository.save(new Product("Phone B1", "Samsung smartphone", 399.99f));
		productRepository.save(new Product("phone B2", "Huawei smartphone", 259.99f));
		productRepository.save(new Product("phone b3", "Apple smartphone", 799.99f));
		productRepository.save(new Product("fone b3", "Xiaomi smartphone", 299.99f));

		List<Product> products = productRepository.findProductsByNameCheaperThan("phone", 300);

		assertEquals(1, products.size());
	}

	@Test
	public void testFindProductsByNameMoreExpensiveThan(){
		productRepository.save(new Product("Phone B1", "Samsung smartphone", 399.99f));
		productRepository.save(new Product("phone B2", "Huawei smartphone", 259.99f));
		productRepository.save(new Product("phone b3", "Apple smartphone", 799.99f));
		productRepository.save(new Product("fone b3", "Xiaomi smartphone", 299.99f));

		List<Product> products = productRepository.findProductsByNameMoreExpensiveThan("phone", 300);

		assertEquals(2, products.size());
	}

	@Test
	public void testFindProductsByNameBetweenPrices(){
		productRepository.save(new Product("Phone B1", "Samsung smartphone", 399.99f));
		productRepository.save(new Product("phone B2", "Huawei smartphone", 259.99f));
		productRepository.save(new Product("phone b3", "Apple smartphone", 799.99f));
		productRepository.save(new Product("fone b3", "Xiaomi smartphone", 299.99f));

		List<Product> products = productRepository.findProductsByNameBetweenPrices("phone", 200, 500);

		assertEquals(2, products.size());
	}

	@Test
	public void testFindProductByNameWithReviews(){
		// SQL scripts creates and populates the database

		productRepository.save(new Product("Camera B1", "Samsung camera", 399.99f));
		productRepository.save(new Product("camera B2", "Huawei camera", 259.99f));

		List<Product> products = productRepository.findProductByNameWithReviews("camera");

		assertEquals(1, products.size());
	}

	@Test
	public void testFindReviewsByProductId() throws ParseException {
		// Populate the database with product and reviews.
		Product newProduct = new Product("Phone B1", "Samsung smartphone", 399.99f);
		productRepository.save(newProduct);
		int productId = newProduct.getProductId();

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
		List<Review> reviews = reviewRepository.findReviewsByProductId(productId);

		assertEquals(3, reviews.size());
	}

	@Test
	public void testFindReviewsByProductIdRatingAtLeast() throws ParseException {
		// Populate the database with product and reviews.
		Product newProduct = new Product("Phone B1", "Samsung smartphone", 399.99f);
		productRepository.save(newProduct);
		int productId = newProduct.getProductId();

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
		List<Review> reviews = reviewRepository.findReviewsByProductIdRatingAtLeast(productId, 4);

		assertEquals(2, reviews.size());
	}

	@Test
	public void testFindReviewsByProductIdSinceDate() throws ParseException {
		// Populate the database with product and reviews.
		Product newProduct = new Product("Phone B1", "Samsung smartphone", 399.99f);
		productRepository.save(newProduct);
		int productId = newProduct.getProductId();

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
		Date date = new SimpleDateFormat("yyyy/MM/dd").parse("2019/03/01");
		List<Review> reviews = reviewRepository.findReviewsByProductIdSinceDate(productId, date);

		assertEquals(1, reviews.size());
	}

	@Test
	public void testFindCommentsByReviewId() throws ParseException {
		// Populate the database with product and reviews.
		Product newProduct = new Product("Phone B1", "Samsung smartphone", 399.99f);
		productRepository.save(newProduct);
		int productId = newProduct.getProductId();

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

		int reviewId1 = review1.getReviewId();
		int reviewId2 = review2.getReviewId();

		Comment comment1 = new Comment("I agree with you", date1, review1);
		Comment comment2 = new Comment("I agree with you", date2, review1);
		Comment comment3 = new Comment("I agree with you", date3, review1);

		commentRepository.save(comment1);
		commentRepository.save(comment2);
		commentRepository.save(comment3);

		// Query the comments
		List<Comment> comments1 = commentRepository.findCommentsByReviewId(reviewId1);
		List<Comment> comments2 = commentRepository.findCommentsByReviewId(reviewId2);

		assertEquals(3, comments1.size());
		assertEquals(0, comments2.size());
	}

}