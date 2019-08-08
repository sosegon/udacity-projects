/*
 Use MySQL as DML to populate the tables.
*/

/*
 Products
*/
INSERT INTO products (name, description)
VALUES ('Camera A', 'Digital camera with 3 megapixels');

INSERT INTO products (name, description)
VALUES ('Laptop A', 'Laptop 17"');

INSERT INTO products (name, description)
VALUES ('Mouse A', '3-button mouse for gaming');

/*
 Reviews
*/
INSERT INTO reviews (content, date_creation, product_id)
VALUES ('A very good camera for beginners', NOW(), 1);
INSERT INTO reviews (content, date_creation, product_id)
VALUES ('Good enough for the price', NOW(), 1);

INSERT INTO reviews (content, date_creation, product_id)
VALUES ('Nice laptop for heavy-weight work', NOW(), 2);

INSERT INTO reviews (content, date_creation, product_id)
VALUES ('Not the best mouse in the market', NOW(), 3);

/*
 Comments
*/
INSERT INTO comments (content, date_creation, review_id)
VALUES ('It is difficult to use', NOW(), 1);
INSERT INTO comments (content, date_creation, review_id)
VALUES ('I agree', NOW(), 1);
INSERT INTO comments (content, date_creation, review_id)
VALUES ('It is a good camera', NOW(), 1);

INSERT INTO comments (content, date_creation, review_id)
VALUES ('I do not like the price', NOW(), 2);

INSERT INTO comments (content, date_creation, review_id)
VALUES ('Too heavy', NOW(), 3);
