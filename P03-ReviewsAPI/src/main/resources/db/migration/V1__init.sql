/*
 Use MySQL as DDL to define the tables of the database and their relationships.
*/

/*
 products is the main table of the database.
*/
CREATE TABLE products (
    product_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(300) NOT NULL,
    description TEXT,
    CONSTRAINT product_pk PRIMARY KEY (product_id)
);

/*
 Reviews are related to products. It is a one to many relationship, where one
 product can have many reviews. Accomplishing this requires to set a foreign key
 and a constraint.
*/
CREATE TABLE reviews (
    review_id INT NOT NULL AUTO_INCREMENT,
    content TEXT NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    product_id INT NOT NULL,
    CONSTRAINT review_pk PRIMARY KEY (review_id),
    CONSTRAINT review_product_fk FOREIGN KEY (product_id) REFERENCES products(product_id)
);

/*
 Similarly, comments are related to reviews. It is a one to many relationship,
 where one review can have many reviews. It also requires to set a foreign key
 and a constraint.
*/
CREATE TABLE comments (
    comment_id INT NOT NULL AUTO_INCREMENT,
    content TEXT NOT NULL,
    date_creation TIMESTAMP NOT NULL,
    review_id INT NOT NULL,
    CONSTRAINT comment_pk PRIMARY KEY (comment_id),
    CONSTRAINT comment_review_fk FOREIGN KEY (review_id) REFERENCES reviews(review_id)
);
