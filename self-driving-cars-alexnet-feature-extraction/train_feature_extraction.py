import pickle
import tensorflow as tf
#from sklearn.model_selection import train_test_split
from alexnet import AlexNet

#####################################################################
# Load traffic signs data.
with open("train.p", mode='rb') as f:
	train = pickle.load(f)

X_train, y_train = train['features'], train['labels']

#####################################################################
# Split data into training and validation sets.
from sklearn.cross_validation import train_test_split
X_train, X_validation, y_train, y_validation = train_test_split(X_train, y_train, test_size=0.3, random_state=50)

#####################################################################
# Define placeholders and resize operation.
x = tf.placeholder(tf.float32, (None, 32, 32, 3), name="input_images")
y = tf.placeholder(tf.int32, (None), name="output_labels")

x_resized = tf.image.resize_images(x, [227, 227])

one_hot_y = tf.one_hot(y, 43) #  one hot-encoding the labels.

def Nnet(x):
	# TODO: pass placeholder as first argument to `AlexNet`.
	fc7 = AlexNet(x, feature_extract=True)
	# NOTE: `tf.stop_gradient` prevents the gradient from flowing backwards
	# past this point, keeping the weights before and up to `fc7` frozen.
	# This also makes training faster, less work to do!
	fc7 = tf.stop_gradient(fc7)

	# TODO: Add the final layer for traffic sign classification.
	n_classes = 43
	shape = (fc7.get_shape().as_list()[-1], n_classes)
	weights = tf.Variable(tf.truncated_normal(shape))
	biases = tf.Variable(tf.zeros(n_classes))

	fc8 = tf.add(tf.matmul(fc7, weights), biases)

	probs = tf.nn.softmax(fc8)

	return probs

#####################################################################
# Define loss, training, accuracy operations.
# HINT: Look back at your traffic signs project solution, you may
# be able to reuse some the code.

EPOCHS = 10
BATCH_SIZE = 128
rate = 0.001
logits = Nnet(x_resized)
cross_entropy = tf.nn.softmax_cross_entropy_with_logits(logits, one_hot_y)
loss_operation = tf.reduce_mean(cross_entropy)
optimizer = tf.train.AdamOptimizer(learning_rate = rate)
training_operation = optimizer.minimize(loss_operation)

#####################################################################
# Train and evaluate the feature extraction model.

correct_prediction = tf.equal(tf.argmax(logits, 1), tf.argmax(one_hot_y, 1)) #  1st step, measures if a given prediction is correct, it compares a logit and the one_hot encoded ground-truth label
accuracy_operation = tf.reduce_mean(tf.cast(correct_prediction, tf.float32)) # 2nd step, calculates the overall prediction accuracies by averaging the individual prediction accuracies

saver = tf.train.Saver()

def evaluate(X_data, y_data): # runs the evaluation pipeline
    num_examples = len(X_data)
    total_accuracy = 0
    sess = tf.get_default_session()
    for offset in range(0, num_examples, BATCH_SIZE):
        batch_x, batch_y = X_data[offset:offset+BATCH_SIZE], y_data[offset:offset+BATCH_SIZE]
        accuracy = sess.run(accuracy_operation, feed_dict={x: batch_x, y: batch_y})
        total_accuracy += (accuracy * len(batch_x))
    return total_accuracy / num_examples # total accuracy of the model

#####################################################################
from datetime import datetime as dt
from sklearn.utils import shuffle

save_file = 'train_model'
log_file =  "log.txt"
log = ""
with tf.Session() as sess:
    start = dt.now()
    sess.run(tf.global_variables_initializer())
    num_examples = len(X_train)
    
    print("Training...")
    print()
    for i in range(EPOCHS):
        e_start = dt.now()
        X_train, y_train = shuffle(X_train, y_train) # shuffle to ensure that the training data is not biased by the order of the images
        for offset in range(0, num_examples, BATCH_SIZE):
            end = offset + BATCH_SIZE
            batch_x, batch_y = X_train[offset:end], y_train[offset:end]
            sess.run(training_operation, feed_dict={x: batch_x, y: batch_y})
            
        validation_accuracy = evaluate(X_validation, y_validation)
        
        e_end = dt.now()
        e_delta = e_end - e_start
        e_total = round(e_delta.seconds + e_delta.microseconds/1E6, 2)
        print("EPOCH {} ...".format(i+1))
        print("Validation Accuracy = {:.3f}".format(validation_accuracy))
        print("Time = {:.2f}".format(e_total))
        print()
        log += "\n\nEPOCH {} ...".format(i+1)
        log += "\nValidation Accuracy = {:.3f}".format(validation_accuracy)
        log += "\nTime: " + str(e_total)+ " seconds"
        
    saver.save(sess, save_file)
    print("Model saved")
    end = dt.now()
    delta = end - start
    total = round(delta.seconds + delta.microseconds/1E6, 2) # from http://stackoverflow.com/a/2880735/1065981
    log += "\n\nTotal time: " + str(total) + " seconds"
    print("Total time = {:.2f}".format(total))
    
    with open(log_file, 'w+') as f:
        f.write(log)
        f.close()
