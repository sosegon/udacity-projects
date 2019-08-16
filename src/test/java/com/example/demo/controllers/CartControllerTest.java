package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
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
public class CartControllerTest {

    private CartController cartController;

    @Autowired
    private UserRepository userRepo;

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
        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepo);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepo);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepo);

        testUser = userRepo.findByUsername(testUsername);
        if(testUser == null) {
            testUser = new User();
            testUser.setUsername(testUsername);
            testUser.setPassword(testPassword);
            userRepo.save(testUser);
        }

        List<Item> items = itemRepo.findByName(testName);

        if(items.size() == 0) {
            testItem = new Item();
            testItem.setName(testName);
            testItem.setDescription(testDesc);
            testItem.setPrice(testPrice);
            itemRepo.save(testItem);
        }

        testCart = new Cart();
        testCart.setUser(testUser);
        testCart.addItem(testItem);
        testUser.setCart(testCart);
        cartRepo.save(testCart);
    }

    @Test
    public void addToCartFailsDueToNonexistingUser() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("nonExisting");

        ResponseEntity<Cart> response = cartController.addTocart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void addToCartFailsDueToNonexistingItem() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(testUsername);
        request.setItemId(1001L);

        ResponseEntity<Cart> response = cartController.addTocart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void addToCartSuccess() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(testUsername);
        request.setItemId(testItem.getId());
        int quantity = 2;
        request.setQuantity(quantity);

        ResponseEntity<Cart> response = cartController.addTocart(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testItem.getPrice().multiply(new BigDecimal(quantity + 1)), response.getBody().getTotal());
    }

    @Test
    public void removeFromCartFailsDueToNonexistingUser() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername("nonExisting");

        ResponseEntity<Cart> response = cartController.removeFromCart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void removeFromCartFailsDueToNonexistingItem() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(testUsername);
        request.setItemId(1001L);

        ResponseEntity<Cart> response = cartController.removeFromCart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void removeFromCartSuccess() {
        ModifyCartRequest request = new ModifyCartRequest();
        request.setUsername(testUsername);
        request.setItemId(testItem.getId());
        int quantity = 1;
        request.setQuantity(quantity);

        ResponseEntity<Cart> response = cartController.removeFromCart(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testItem.getPrice().multiply(new BigDecimal(quantity - 1)), response.getBody().getTotal());
    }
}
