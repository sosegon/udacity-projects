
from keras.models import Sequential
from keras.layers import Dense, Dropout, Flatten, ELU, Lambda
from keras.layers.convolutional import Convolution2D
from keras.optimizers import Adam
import pandas
import matplotlib.pyplot as plt
import numpy as np
import cv2
from random import randrange
import json
import tensorflow as tf
from sklearn.utils import shuffle
from sklearn.cross_validation import train_test_split

flags = tf.app.flags
FLAGS = flags.FLAGS

flags.DEFINE_integer('epochs', 5, 'Number of epochs.')

flags.DEFINE_integer('samples_epoch', 500, 'Samples in every epoch.')
flags.DEFINE_integer('batch_size', 50, 'Size of batch in training.')
flags.DEFINE_integer('zeros_drop', 90, 'Percentage of zero steering to drop.')
flags.DEFINE_float('learning', 0.001, 'Learning rate.')

flags.DEFINE_string('draw_images', 'yes', 'Draw images in process')

recovery_threshold = 0.25
brightness_ex = None
nbrightness_ex = None
nflip_ex = None
flip_ex = None

final_width = 200
final_height = 66
final_depth = 3

crop_top_percentage = 35
crop_bottom_percentage = 15

def generator(Xpath, y, batch_size, draw=False):
	total_input = len(Xpath)

	global brightness_ex
	global nbrightness_ex
	global flip_ex
	global nflip_ex

	while True:
		features, targets = [], []
		i = 0
		#index = 0
		#Xpath = shuffle(Xpath)
		while len(features) < batch_size:
			index = randrange(0, total_input)
			path = Xpath[index]
			#index += 1
			
			angle = y[index]
			image = read_image(path)
			image = crop_top_bottom(image, crop_top_percentage, crop_bottom_percentage)

			image = resize(image, final_width, final_height)

			features.append(image)
			targets.append(angle)
			i += 1

			if i % 3 == 0:
				if nbrightness_ex is None and draw is True:
					nbrightness_ex = np.copy(image)
					save_image(nbrightness_ex, "normal_brightness.jpg")
				
				image = change_brightness(image)
				
				if brightness_ex is None and draw is True:
					brightness_ex = np.copy(image)
					save_image(brightness_ex, "change_brightness.jpg")
			
			elif i % 3 == 1:
				if nflip_ex is None and draw is True:
					nflip_ex = np.copy(image)
					save_image(nflip_ex, "normal_flip.jpg")

				image = flip_image(image)
				angle *= -1

				if flip_ex is None and draw is True:
					flip_ex = np.copy(image)
					save_image(flip_ex, "change_flip.jpg")
			
			features.append(image)
			targets.append(angle)
			i += 1

		yield (np.array(features), np.array(targets))

def save_image(image_array, file_name):
	cv2.imwrite(file_name, image_array)

def read_image(path):
	return cv2.imread(path)

def flip_image(image):
	return cv2.flip(image, 1)

def change_brightness(image):
	hsv = cv2.cvtColor(image, cv2.COLOR_RGB2HSV)
	hsv[:,:,2] = hsv[:,:,2] * (0.25 + np.random.uniform()) 
	return cv2.cvtColor(hsv, cv2.COLOR_HSV2RGB)

def crop_top_bottom(image, percentage_top, percentage_bottom):
	height = image.shape[0]
	crop_size_top = int(height * percentage_top / 100)
	crop_size_bottom = int(height * percentage_bottom / 100)
	return image[crop_size_top:height-crop_size_bottom,:,:]

def resize(image, width, height):
	return cv2.resize(image, (width, height), interpolation=cv2.INTER_CUBIC)

def extract_saturation(image):
	hsv = cv2.cvtColor(image, cv2.COLOR_RGB2HSV)
	img = hsv[:,:,1]
	img = np.expand_dims(img, axis=2)
	return img

def get_model(learning):

	image_shape = (final_height, final_width, final_depth)

	model = Sequential()
	model.add(Lambda(lambda x: x/127.5 - 1., input_shape=image_shape, output_shape=image_shape))
	
	model.add(Convolution2D(24, 5, 5, subsample=(2, 2), border_mode="valid"))
	model.add(ELU())
	
	model.add(Convolution2D(36, 5, 5, subsample=(2, 2), border_mode="valid"))
	model.add(ELU())
	
	model.add(Convolution2D(48, 5, 5, subsample=(2, 2), border_mode="valid"))
	model.add(ELU())
	
	model.add(Convolution2D(64, 3, 3, border_mode="valid"))
	model.add(ELU())
	
	model.add(Convolution2D(64, 3, 3, border_mode="valid"))
	model.add(ELU())
	
	model.add(Flatten())

	model.add(Dense(100))
	model.add(ELU())

	model.add(Dense(50))
	model.add(ELU())

	model.add(Dense(10))
	model.add(ELU())

	model.add(Dense(1))

	optimizer = Adam(lr=learning)
	model.compile(optimizer=optimizer, loss="mse")

	return model

def draw_steering_histogram(data, name):
	plt.clf()
	plt.hist(data, np.linspace(-2, 2))
	plt.xlabel("steering angles")
	plt.title("Steering angle distribution")
	plt.savefig(name)

def load_data(percetange_zero_drop, draw=False):
	# trim white spaces according to http://stackoverflow.com/a/33790933/1065981
	df = pandas.read_csv("driving_log.csv", delimiter=" *, *")
	# center left right steering throttle brake speed
	dataset = df.values
	angles = dataset[:,3]

	if draw is True:
		draw_steering_histogram(angles, "hist_steering_full")
	
	zero_steering_records_to_keep = int(len(angles[angles == 0.0]) * (100-percetange_zero_drop)/100)

	print("zeros to keep:" + str(zero_steering_records_to_keep))


	#Remove records for 0 steering, just keep some of them
	df_zeros = df.drop(df.index[df.steering != 0])
	df_zeros = df.sample(zero_steering_records_to_keep)
	df = df.drop(df.index[df.steering == 0])
	df = pandas.concat([df, df_zeros])

	dataset = df.values
	angles = dataset[:,3]

	if draw is True:
		draw_steering_histogram(angles, "hist_steering_drop_zeros")

	X_c, X_l, X_r = dataset[:,0], dataset[:,1], dataset[:,2]

	y_c = dataset[:,3]
	y_l = dataset[:,3] + recovery_threshold
	y_r = dataset[:,3] - recovery_threshold

	X = np.concatenate((X_c, X_l, X_r))
	y = np.concatenate((y_c, y_l, y_r))

	if draw is True:
		draw_steering_histogram(y, "hist_steering_total")

	assert len(X) == len(y)

	X, y = shuffle(X, y)
	
	return X, y

def plot_metrics(history):
	keys = history.history.keys()

	for k in keys:
		plt.clf()
		plt.plot(history.history[k])
		plt.title("model " + k)
		plt.ylabel(k)
		plt.xlabel('epoch')
		plt.savefig("plot_" + k + ".jpg")

def getFeatureTargets(Xpath, y):
	feats = []
	targets = []
	for path, angle in zip(Xpath, y):
		image = read_image(path)
		image = crop_top_bottom(image, 35, 15)
		image = resize(image, final_width, final_height)
		feats.append(image)
		targets.append(angle)

	return np.array(feats), np.array(targets)

def main(_):
	draw = False
	if FLAGS.draw_images.strip().lower() == 'yes':
		draw = True

	Xpath, y = load_data(FLAGS.zeros_drop, draw)
	Xpath, XXpath, y, yy = train_test_split(Xpath, y, test_size=0.3, random_state=50)

	print("Number of images: " + str(len(Xpath)))

	model = get_model(FLAGS.learning)
	print(model.summary())
	history = model.fit_generator(
		generator(Xpath, y, FLAGS.batch_size),
		samples_per_epoch = FLAGS.samples_epoch,
		validation_data = getFeatureTargets(XXpath, yy),
		nb_epoch=FLAGS.epochs
	)
	if draw is True:
		plot_metrics(history)

	model.save_weights("model.h5")
	with open("model.json", "w+") as outfile:
		json.dump(model.to_json(), outfile)


if __name__ == "__main__":
	tf.app.run()
	
	