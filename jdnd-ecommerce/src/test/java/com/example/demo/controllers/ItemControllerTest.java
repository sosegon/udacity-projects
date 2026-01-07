package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
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
public class ItemControllerTest {

    private ItemController itemController;

    @Autowired
    private ItemRepository itemRepo;

    private Item testItem;
    private String testName = "testItem";
    private BigDecimal testPrice = new BigDecimal(100.10);
    private String testDesc = "testDescription";

    @Before
    public void setUp() {
        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepo);

        List<Item> items = itemRepo.findByName(testName);

        if(items.size() == 0) {
            testItem = new Item();
            testItem.setName(testName);
            testItem.setDescription(testDesc);
            testItem.setPrice(testPrice);

            itemRepo.save(testItem);
        }
    }

    @Test
    public void getItems() {
        ResponseEntity<List<Item>> response = itemController.getItems();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void getItemById() {
        ResponseEntity<Item> response = itemController.getItemById(testItem.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testItem.getId(), response.getBody().getId());

        response = itemController.getItemById(1001L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getItemsByName() {
        ResponseEntity<List<Item>>response = itemController.getItemsByName(testName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testItem.getId(), response.getBody().get(0).getId());

        response = itemController.getItemsByName("nonExisting");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
