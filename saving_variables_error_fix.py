import tensorflow as tf

# Remove the previous weights and bias
tf.reset_default_graph()

save_file = 'model.ckpt'

# Two Variables: weights and bias
# This gives an error because TF is trying to assing
# weight data into bias and bias data into weight
# bias = tf.Variable(tf.truncated_normal([3]))
# weights = tf.Variable(tf.truncated_normal([2, 3]))
# Fix that assigning names manually
weights = tf.Variable(tf.truncated_normal([2, 3]), name='weights_0')
bias = tf.Variable(tf.truncated_normal([3]), name='bias_0')
saver = tf.train.Saver()

# Print the name of Weights and Bias
print('Save Weights: {}'.format(weights.name))
print('Save Bias: {}'.format(bias.name))

with tf.Session() as sess:
    sess.run(tf.initialize_all_variables())
    saver.save(sess, save_file)

# Remove the previous weights and bias
tf.reset_default_graph()

# Two Variables: weights and bias
# This gives an error because TF is trying to assing
# weight data into bias and bias data into weight
# bias = tf.Variable(tf.truncated_normal([3]))
# weights = tf.Variable(tf.truncated_normal([2, 3]))
# Fix that assigning names manually
weights = tf.Variable(tf.truncated_normal([2, 3]), name='weights_0')
bias = tf.Variable(tf.truncated_normal([3]), name='bias_0')

saver = tf.train.Saver()

# Print the name of Weights and Bias
print('Load Weights: {}'.format(weights.name))
print('Load Bias: {}'.format(bias.name))

with tf.Session() as sess:
    # Load the weights and bias - ERROR
    saver.restore(sess, save_file)

print('Loaded Weights and Bias successfully.')