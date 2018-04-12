####################################################################################

# Load pickled data
from keras.datasets import cifar10
(X_train, y_train), (X_test, y_test) = cifar10.load_data()
y_train = y_train.squeeze()
y_test = y_test.squeeze()
print(y_train.shape)
# import pickle

# training_file = "train.p"
# testing_file = "test.p"

# with open(training_file, mode='rb') as f:
#     train = pickle.load(f)
# with open(testing_file, mode='rb') as f:
#     test = pickle.load(f)
    
# X_train, y_train = train['features'], train['labels']
# X_test, y_test = test['features'], test['labels']
####################################################################################
import numpy as np
### Replace each question mark with the appropriate value.
n_train = len(X_train)
n_test = len(X_test)
image_shape = X_train[0].shape
n_classes = len(np.unique(y_train))

print("Number of training examples =", n_train)
print("Number of testing examples =", n_test)
print("Image data shape =", image_shape)
print("Number of classes =", n_classes)
####################################################################################
#Shuffle data and split dataset into train and validation
from sklearn.utils import shuffle
from sklearn.cross_validation import train_test_split

X_train, y_train = shuffle(X_train, y_train)
X_train, X_validation, y_train, y_validation = train_test_split(X_train, y_train, test_size=0.3, random_state=50)

n_train = len(X_train)

n_val = len(X_validation)

print("Number of training examples =", n_train)
print("Number of validation examples =", n_val)
####################################################################################
### Preprocess the data here.
import cv2

def preprocess_image(x):

    # convert to grayscale
    gray = cv2.cvtColor(x, cv2.COLOR_BGR2GRAY)
    
    # increase contrast
    contrast = cv2.equalizeHist(gray)
    
    # make dimensions 32x32x1, not 32x32
    contrast = np.expand_dims(contrast, axis=2)
    
    return contrast

p_images = []
for img in X_train:
    new_img = preprocess_image(img)
    p_images.append(new_img)
    
X_train = np.array(p_images)
####################################################################################
# Convert to grayscale the validation and testing sets
# so the images in these groups have the same
# dimensions as the ones in the training set
def gray_image(x):

    # convert to grayscale
    gray = cv2.cvtColor(x, cv2.COLOR_BGR2GRAY)
      
    # make dimensions 32x32x1, not 32x32
    gray = np.expand_dims(gray, axis=2)
    
    return gray

p_images = []
for img in X_validation:
    new_img = preprocess_image(img)
    p_images.append(new_img)
    
X_validation = np.array(p_images)

p_images = []
for img in X_test:
    new_img = preprocess_image(img)
    p_images.append(new_img)
    
X_test = np.array(p_images)

n_val = len(X_validation)
n_test = len(X_test)

print("Number of validation examples =", n_val)
print("Number of testing examples =", n_test)
####################################################################################
### Generate data additional data (OPTIONAL!)
def blur_image(x):
    x = np.squeeze(x)
    x = cv2.GaussianBlur(x, (3, 3), 0)
    x = np.expand_dims(x, axis=2)
    return x

def translate_image(x):
    x = np.squeeze(x)
    delta = random.randrange(1, 3) # 1 or 2 pixels
    rows, cols = x.shape
    M = np.float32([[1, 0, delta], [0, 1, delta]])
    t = cv2.warpAffine(x, M, (cols, rows))
    t = np.expand_dims(t, axis=2)
    
    return t

def rotate_image(x):
    x = np.squeeze(x)
    delta = random.randrange(1, 16) # between 0 - 15 degrees
    rows, cols = x.shape
    M = cv2.getRotationMatrix2D((cols/2,rows/2),delta,1)
    r = cv2.warpAffine(x, M, (cols, rows))
    r = np.expand_dims(r, axis=2)
    
    return r

X_big, X_small, y_big, y_small = train_test_split(X_train, y_train, test_size=0.8, random_state=50)

import random

#Add 50% of transformed images
for i in range(1):
    extra_data = []
    for img in X_small:
        ran = random.randrange(1, 4) # 1, 2, 3
        new_image = None
        if ran == 1:
            new_image = blur_image(translate_image(img))
        elif ran == 2:
            new_image = rotate_image(blur_image(img))
        elif ran == 3:
            new_image = rotate_image(translate_image(img))

        extra_data.append(new_image)
    
    X_train = np.append(X_train, np.array(extra_data), axis=0)
    y_train = np.append(y_train, y_small, axis=0)

# same number of inputs and outpus
assert len(X_train) == len(y_train)

n_train = len(X_train)
print("Number of training examples =", n_train)
print("Input shape =", X_train.shape)
####################################################################################
# Balance the data
n_elem_class_train = np.bincount(y_train) # number of elements per class in training set
avg_elem_class_train = round(np.average(n_elem_class_train))

# Remove elements from classes with more elements than the average
elements_to_remove_per_class = []
for n_elem in n_elem_class_train:
    if n_elem > avg_elem_class_train:
        elements_to_remove_per_class.append(n_elem - avg_elem_class_train)
    else:
        elements_to_remove_per_class.append(0)

indices_elements_to_remove = []
index = 0
for clazz in y_train:
    if elements_to_remove_per_class[clazz] > 0:
        indices_elements_to_remove.append(index)
        elements_to_remove_per_class[clazz] -= 1
    index += 1

y_train = np.delete(y_train, indices_elements_to_remove)
X_train = np.delete(X_train, indices_elements_to_remove, axis=0)

n_train = len(X_train)
print("Number of training examples =", n_train)
print("Input shape =", X_train.shape)
####################################################################################
# Normalize RGB values
def normalize(x):
    x = x.astype(float)
    max_i = 255
    min_i = 0
    max_o = 1
    min_o = 0

    return ((x - min_i) * (max_o - min_o) / (max_i - min_i)) + min_o

def normalize_set(setImages):
    
    normalized_images = []
    for img in setImages:
        normalized_images.append(normalize(img))

    return np.array(normalized_images)

X_train = normalize_set(X_train)
X_test = normalize_set(X_test)
X_validation = normalize_set(X_validation)
####################################################################################
### Define your architecture here.
### Feel free to use as many code cells as needed.
import tensorflow as tf

def CNNet(x):
    
    mu = 0
    sigma = 0.10
        
    ####################################################
    # Convolution 1 (32x32x1) -> (28x28x8)
    w, h, d, n = 5, 5, 1, 8
    net1_weights_cv1 = tf.Variable(tf.truncated_normal([w, h, d, n], mean=mu, stddev=sigma), name="net1_weights_cv1")
    net1_biases_cv1 = tf.Variable(tf.zeros(n), name="net1_biases_cv1")
    cv1_w = tf.nn.conv2d(x, net1_weights_cv1, strides=[1, 1, 1, 1], padding='VALID')
    cv1_b = tf.nn.bias_add(cv1_w, net1_biases_cv1)
    
    # Activation 1
    cv1_a = tf.nn.relu(cv1_b)
    
    # Max Pooling 1 (28x28x8) -> (24x24x8)
    n = 5
    cv1_mp = tf.nn.max_pool(cv1_a, [1, n, n, 1], [1, 1, 1, 1], 'VALID')
    
    ####################################################
    # Convolution 2 (24x24x8) -> (20x20x16)
    w, h, d, n = 5, 5, 8, 16
    net1_weights_cv2 = tf.Variable(tf.truncated_normal([w, h, d, n], mean=mu, stddev=sigma),name="net1_weights_cv2")
    net1_biases_cv2 = tf.Variable(tf.zeros(n), name="net1_biases_cv2")
    cv2_w = tf.nn.conv2d(cv1_mp, net1_weights_cv2, strides=[1, 1, 1, 1], padding='VALID')
    cv2_b = tf.nn.bias_add(cv2_w, net1_biases_cv2)
    
    # Activation 2
    cv2_a = tf.nn.relu(cv2_b)
    
    # Max Pooling 2 (20x20x16) -> (16x16x16)
    n = 5
    cv2_mp = tf.nn.max_pool(cv2_a, [1, n, n, 1], [1, 1, 1, 1], 'VALID')

    ####################################################
    # Convolution 3 (16x16x16) -> (8x8x20)
    w, h, d, n = 9, 9, 16, 20
    net1_weights_cv3 = tf.Variable(tf.truncated_normal([w, h, d, n], mean=mu, stddev=sigma), name="net1_weights_cv3")
    net1_biases_cv3 = tf.Variable(tf.zeros(n), name="net1_biases_cv3")
    cv3_w = tf.nn.conv2d(cv2_mp, net1_weights_cv3, strides=[1, 1, 1, 1], padding='VALID')
    cv3_b = tf.nn.bias_add(cv3_w, net1_biases_cv3)
    
    # Activation 3
    cv3_a = tf.nn.relu(cv3_b)
    
    # Max Pooling 3 (8x8x20) -> (4x4x20)
    n = 5
    cv3_mp = tf.nn.max_pool(cv3_a, [1, n, n, 1], [1, 1, 1, 1], 'VALID')

    ####################################################
    # Flatten (4x4x20) -> 320
    flat = tf.contrib.layers.flatten(cv3_mp)
        
    ####################################################
    # Fully connected 1 320 -> 200
    i, o = 320, 200
    net1_weights_fc1 = tf.Variable(tf.truncated_normal([i, o], mean=mu, stddev=sigma), name="net1_weights_fc1")
    net1_biases_fc1 = tf.Variable(tf.zeros(o), name="net1_biases_fc1")
    fc1_wb = tf.add(tf.matmul(flat, net1_weights_fc1), net1_biases_fc1)
    
    # Activation 4
    fc1_a = tf.nn.sigmoid(fc1_wb)
    
    ####################################################
    # Fully connected 2 200 -> 43
    i, o = 200, 43
    net1_weights_fc2 = tf.Variable(tf.truncated_normal([i, o], mean=mu, stddev=sigma), name="net1_weights_fc2")
    net1_biases_fc2 = tf.Variable(tf.zeros(o), name="net1_biases_fc2")
    final = tf.add(tf.matmul(fc1_a, net1_weights_fc2), net1_biases_fc2)
    
    return final
####################################################################################
EPOCHS = 20
BATCH_SIZE = 128 # The larger the batch size, the faster the model will train

x = tf.placeholder(tf.float32, (None, 32, 32, 1), name="input_images")  # it is a placeholder to store the input patches. None allows the placeholder to accept a batch of any size. 32, 32, 1 is the image dimensions.
y = tf.placeholder(tf.int32, (None), name="output_labels") # stores the labels, they are not one hot-encoding yet.
one_hot_y = tf.one_hot(y, 43) #  one hot-encoding the labels.
####################################################################################
rate = 0.001
logits = CNNet(x)
cross_entropy = tf.nn.softmax_cross_entropy_with_logits(logits, one_hot_y)
loss_operation = tf.reduce_mean(cross_entropy)
optimizer = tf.train.AdamOptimizer(learning_rate = rate)
training_operation = optimizer.minimize(loss_operation)
####################################################################################
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
####################################################################################
### Train your model here.
### Feel free to use as many code cells as needed
from datetime import datetime as dt

save_file = 'train_model3'
log_file =  "log3.txt"
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
####################################################################################