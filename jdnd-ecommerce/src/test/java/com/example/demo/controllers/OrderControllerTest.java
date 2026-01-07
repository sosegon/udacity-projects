package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderControllerTest {

    private OrderController orderController;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private CartRepository cartRepo;

    @Autowired
    private ItemRepository itemRepo;

    private Cart testCart;

    private User testUser;
    private String testUsername = "testUsername";
    private String testPassword = "testPassword";

    private Item testItem;
    private String testName = "testItem";
    private BigDecimal testPrice = new BigDecimal(100.10);
    private String testDesc = "testDescription";

    @Before
    public void setUp() {
        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "userRepository", userRepo);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepo);

        testUser = userRepo.findByUsername(testUsername);
        if(testUser == null) {
            testUser = new User();
            testUser.setUsername(testUsername);
            testUser.setPassword(testPassword);
            userRepo.save(testUser);
        }

        testItem = new Item();
        testItem.setName(testName);
        testItem.setDescription(testDesc);
        testItem.setPrice(testPrice);
        itemRepo.save(testItem);

        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.addItem(testItem);
        testUser.setCart(testCart);
        cartRepo.save(testCart);
    }

    @Test
    public void submitFailsDueToNonexistingUser() {
        ResponseEntity<UserOrder> response = orderController.submit("nonExisting");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, orderRepo.findAll().size());
    }

    @Test
    public void submitSuccess() {
        ResponseEntity<UserOrder> response = orderController.submit(testUsername);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, orderRepo.findAll().size());
    }

    @Test
    public void getOrdersForUserFailsDueToNonexistingUser() {
        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser("nonExisting");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getOrdersForUserSuccess() {
        orderController.submit(testUsername);
        ResponseEntity<List<UserOrder>> response = orderController.getOrdersForUser(testUsername);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
