package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UserControllerTest {

    private UserController userController;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private CartRepository cartRepo;

    private BCryptPasswordEncoder encoder = mock(BCryptPasswordEncoder.class);

    private User testUser;
    private String testUsername = "testUsername";
    private String testPassword = "testPassword";

    @Before
    public void setUp() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "passwordEncoder", encoder);

        testUser = userRepo.findByUsername(testUsername);
        if(testUser == null) {
            testUser = new User();
            testUser.setUsername(testUsername);
            testUser.setPassword(testPassword);
            userRepo.save(testUser);
        }
    }

    @Test
    public void findById() {
        ResponseEntity<User> response = userController.findById(testUser.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser.getId(), response.getBody().getId());

        response = userController.findById(1001L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void findByUserName() {
        ResponseEntity<User> response = userController.findByUserName(testUsername);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser.getId(), response.getBody().getId());

        response = userController.findByUserName("nonExisting");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void createUserSuccess() throws Exception {
        when(encoder.encode("testPassword")).thenReturn("thisIsHashed");

        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("test");
        userRequest.setPassword("testPassword");
        userRequest.setConfirmPassword("testPassword");

        ResponseEntity<User> response = userController.createUser(userRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        User newUser = response.getBody();
        assertNotNull(newUser);
        assertEquals(userRepo.findByUsername(newUser.getUsername()).getId(), newUser.getId());
        assertEquals("thisIsHashed", newUser.getPassword());
    }

    @Test
    public void createUserFailsDueToWrongConfirmPassword() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("test");
        userRequest.setPassword("testPassword");
        userRequest.setConfirmPassword("testPassw0rd");

        ResponseEntity<User> response = userController.createUser(userRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void createUserFailsDueToLengthOfPassword() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setUsername("test");
        userRequest.setPassword("test");
        userRequest.setConfirmPassword("test");

        ResponseEntity<User> response = userController.createUser(userRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
