import tensorflow as tf
import os.path

# The file path to save the data
save_file = 'model.ckpt'

# Remove the previous weights and bias
tf.reset_default_graph()

# Two Tensor Variables: weights and bias
weights = tf.Variable(tf.truncated_normal([2, 3]))
bias = tf.Variable(tf.truncated_normal([3]))

# Class used to save and/or restore Tensor Variables
saver = tf.train.Saver()

with tf.Session() as sess:
    # Initialize all the Variables
    if os.path.exists(save_file):
    	saver.restore(sess, save_file)
    else:
    	sess.run(tf.initialize_all_variables())

    # Show the values of weights and bias
    print('Weights:')
    print(sess.run(weights))
    print('Bias:')
    print(sess.run(bias))

    # Save the model
    saver.save(sess, save_file)