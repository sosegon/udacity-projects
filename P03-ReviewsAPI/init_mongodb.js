db = connect("localhost:27017/reviews");

db.reviews.drop();
db.createCollection("reviews");
db.reviews.insert({
	"_id": 1,
	"content": "A very good camera for beginners",
	"dateCreation": new Date(),
	"rating": 4,
	"productId": 1
});
db.reviews.insert({
	"_id": 2,
	"content": "Good enough for the price",
	"dateCreation": new Date(),
	"rating": 4,
	"productId": 1
});
db.reviews.insert({
	"_id": 3,
	"content": "Nice laptop for heavy-weight work",
	"dateCreation": new Date(),
	"rating": 5,
	"productId": 2
});
db.reviews.insert({
	"_id": 4,
	"content": "Not the best mouse in the market",
	"dateCreation": new Date(),
	"rating": 2,
	"productId": 3
});

db.comments.drop();
db.createCollection("comments");
db.comments.insert({
	"_id": 1,
	"content": "It is difficult to use",
	"dateCreation": new Date(),
	"reviewId": 1
});
db.comments.insert({
	"_id": 2,
	"content": "It agree",
	"dateCreation": new Date(),
	"reviewId": 1
});
db.comments.insert({
	"_id": 3,
	"content": "It is a good camera",
	"dateCreation": new Date(),
	"reviewId": 1
});
db.comments.insert({
	"_id": 4,
	"content": "It do not like the price",
	"dateCreation": new Date(),
	"reviewId": 2
});
db.comments.insert({
	"_id": 5,
	"content": "Too heavy",
	"dateCreation": new Date(),
	"reviewId": 3
});
