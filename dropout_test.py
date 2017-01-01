# Solution is available in the other "solution.py" tab
import tensorflow as tf

output = None
hidden_layer_weights = [
    [0.1, 0.2, 0.4],
    [0.4, 0.6, 0.6],
    [0.5, 0.9, 0.1],
    [0.8, 0.2, 0.8]]
out_weights = [
    [0.1, 0.6],
    [0.2, 0.1],
    [0.7, 0.9]]

# Weights and biases
weights = [
    tf.Variable(hidden_layer_weights),
    tf.Variable(out_weights)]
biases = [
    tf.Variable(tf.zeros(3)),
    tf.Variable(tf.zeros(2))]
keep_prob = tf.placeholder(tf.float32)

# Input
features = tf.Variable([[0.0, 2.0, 3.0, 4.0], [0.1, 0.2, 0.3, 0.4], [11.0, 12.0, 13.0, 14.0]])
fs = features
ws = weights[0]
bs = biases[0]

# TODO: Create Model
hidden_layer = tf.add(tf.matmul(fs, ws), bs)
hidden_layer = tf.nn.relu(hidden_layer,)
hidden_layer = tf.nn.dropout(hidden_layer, keep_prob)
hidden_layer = tf.add(tf.matmul(hidden_layer, weights[1]), biases[1])

# TODO: Print session results
init = tf.initialize_all_variables()
with tf.Session() as sess:
    sess.run(init)
    output = sess.run(hidden_layer, feed_dict = {keep_prob: 0.5})
    print(output)

