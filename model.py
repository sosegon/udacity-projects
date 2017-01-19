
from keras.models import Sequential
from keras.layers import Dense, Dropout, Flatten, ELU, Lambda
from keras.layers.convolutional import Convolution2D
import csv
import matplotlib.pyplot as plt
import numpy as np
import cv2
import random
import json

def generator(Xpath, y, batch_size):
	total_input = len(Xpath)
	print("total images: " + str(total_input))
	while True:
		features, targets = [], []
		while len(features) < batch_size:
			index = random.randrange(0, total_input)
			path = Xpath[index]
			img = read_image(path)
			img = crop_top_bottom(img)
			img = resize(img)
			features.append(img)
			targets.append(y[index])

		yield (np.array(features), np.array(targets))

def read_image(path):
	return cv2.imread(path)

def flip_image(image):
	return cv2.flip(image, 1)

def change_brightness(image):
	hsv = cv2.cvtColor(image, cv2.COLOR_RGB2HSV)
	hsv[:,:,2] = hsv[:,:,2] * (np.random.normal()) 
	return cv2.cvtColor(hsv, cv2.COLOR_HSV2RGB)

def crop_top_bottom(image):
	# original image 160 x 320 x 3
	return image[32:128,:,:] # size 96 x 320 x 3

def resize(image):
	return cv2.resize(image, (200, 66), interpolation=cv2.INTER_CUBIC)

def extract_saturation(image):
	hsv = cv2.cvtColor(image, cv2.COLOR_RGB2HSV)
	img = hsv[:,:,1]
	img = np.expand_dims(img, axis=2)
	return img

def crop_zero_angles(data):
	# only 150 records for 0 angle
	n = 0
	c_n, l_n, r_n, a_n = [], [], [], []
	for c, l, r, a in zip(data[0], data[1], data[2], data[3]):
		if a == 0 and n < 150:
			c_n.append(c)
			l_n.append(l)
			r_n.append(r)
			a_n.append(a)
			n += 1
		elif a != 0:
			c_n.append(c)
			l_n.append(l)
			r_n.append(r)
			a_n.append(a)
		

	return c_n, l_n, r_n, a_n

def visualize_data(data):
	plt.hist(data[3], np.linspace(-.2, .2))
	plt.xlabel("steering angles")
	plt.title("Steering angle distribution")
	plt.show()

def read_data():
	left, right, center, angle =  [], [], [], []
	with open('driving_log.csv', 'rt') as data_file:
		data = csv.reader(data_file, delimiter = ',')
		next(data) # skip column names
		for row in data:
			center.append(row[0].strip())
			left.append(row[1].strip())
			right.append(row[2].strip())
			angle.append(float(row[3].strip()))
			
	return center, left, right, angle

def flat_data(data):
	X = []
	y = []

	for c, l, r, a in zip(data[0], data[1], data[2], data[3]):
		X.append(c)
		y.append(a)

		X.append(l)
		y.append(a + 0.1) # angle correction for recovering left
		
		X.append(r)
		y.append(a - 0.1) # angle correction for recovering right

	return np.array(X), np.array(y)

def get_model():

	image_shape = (66, 200, 3)

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
	model.add(Dense(1164))
	
	model.add(ELU())
	model.add(Dense(100))
	
	model.add(ELU())
	model.add(Dense(50))
	
	model.add(ELU())
	model.add(Dense(10))
	
	model.add(ELU())
	model.add(Dense(1))

	model.compile(optimizer="adam", loss="mse")

	return model

if __name__ == "__main__":
	data = read_data()
	data = crop_zero_angles(data) # remove data that biases to 0 angle
	Xpath, y = flat_data(data)

	samples_epoch = 1000
	samples_val = 100

	model = get_model()
	model.fit_generator(
		generator(Xpath, y, samples_epoch * 4),
		samples_per_epoch = samples_epoch,
		validation_data = generator(Xpath, y, samples_val * 4),
		nb_val_samples = samples_val,
		nb_epoch=4
	)

	model.save_weights("model.h5")
	with open("model.json", "w+") as outfile:
		json.dump(model.to_json(), outfile)
