'''
The MIT License
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
'''
import numpy as np
import sys
from datetime import datetime as dt
import cv2
import argparse


def read_file(file_name):
	return cv2.imread(file_name)

def grayscale(image_array):
	gray_scale = cv2.cvtColor(image_array, cv2.COLOR_BGR2GRAY)
	gray_scale = np.expand_dims(gray_scale, axis = 2) # shape is h x w x 1
	return gray_scale

def convolute(image_array, filters_number, filter_size, stride):
	shape = image_array.shape
	
	# filter can not be greater than the image
	if filter_size > shape[0] | filter_size > shape[1]:
		return None

	filter_width = filter_size
	filter_height = filter_size
	filter_depth = shape[2]

	# Weights and biases for the filters
	# TODO: Any other idea for the weights and biases?
	weights = np.random.normal(0, 1, (filters_number, filter_height, filter_width, filter_depth))
	biases = np.random.normal(0, 1, (filters_number, 1))

	# convolve
	# TODO: I am sure there are numpy functions or tricks to avoid the while loops
	output_height = []

	height_counter = 0 # Moving vertically
	while height_counter + filter_height < shape[0]:
		
		output_width = []

		width_counter = 0 # Moving horizontally
		while width_counter + filter_width < shape[1]:
			
			output_depth = [] # elements of the new features map

			filter_counter = 0
			while filter_counter < filters_number: # apply filters
				current_weight = weights[filter_counter]
				current_bias = biases[filter_counter][0]
				patch = image_array[height_counter:height_counter + filter_height, width_counter:width_counter + filter_width, :]
				output_element = np.max(patch * current_weight) + current_bias
				output_depth.append(output_element)
				filter_counter += 1
			

			output_width.append(output_depth)
			width_counter += stride

		output_height.append(output_width)
		height_counter += stride

	if len(output_height) == 0:
		return None

	output = np.array(output_height)
	return output

def max_pooling(image_array, filter_size, stride):
	shape = image_array.shape

	# filter can not be greater than the image
	if filter_size > shape[0] | filter_size > shape[1]:
		return None

	filter_width = filter_size
	filter_height = filter_size

	# max_pool
	# TODO: simplify the loops
	output_height = []

	height_counter = 0 # Moving vertically
	while height_counter + filter_height < shape[0]:
		
		output_width = []

		width_counter = 0 # Moving horizontally
		while width_counter + filter_width < shape[1]:
			
			output_depth = [] # elements of the new features map

			depth_counter = 0 # iterating the channels
			while depth_counter < shape[2]:
				
				patch = image_array[height_counter:height_counter + filter_height, width_counter:width_counter + filter_width, depth_counter]
				pool_value = np.max(patch)
				output_depth.append(pool_value)

				depth_counter += 1

			output_width.append(output_depth)
			width_counter += stride

		output_height.append(output_width)
		height_counter += stride

	if len(output_height) == 0:
		return None

	output = np.array(output_height)
	return output

def rescale(x, min_o, max_o):
	x = x.astype(float)
	max_i = np.max(x)
	min_i = np.min(x)

	return ((x - min_i) * (max_o - min_o) / (max_i - min_i)) + min_o

def normalize(x):
	x = x.astype(float)
	max_i = 255
	min_i = 0
	max_o = 1
	min_o = 0

	return ((x - min_i) * (max_o - min_o) / (max_i - min_i)) + min_o

def save_image(image_array, file_name):
	cv2.imwrite(file_name, image_array)

def file_name_no_ext(image_file_name):
	name = str(image_file_name)
	r = name[::-1]       		# reverse name
	n = r[r.index(".") + 1:]    # reverse name extensionless
	return n[::-1]

# base code from http://stackoverflow.com/a/31636601/1065981
def padding(image_array, pad):
	shape = image_array.shape # H x W x D
	image_array = image_array.transpose(2, 0, 1).reshape(shape[2], shape[0], shape[1])
	img_arr = np.ndarray((3, shape[0] + 2 * pad, shape[1] + 2 * pad), np.float) 
	shape2 = img_arr.shape # D x H x W

	leftPad = round(float((shape2[2] - shape[1])) / 2)
	rightPad = round(float(shape2[2] - shape[1]) - leftPad)
	topPad = round(float((shape2[1] - shape[0])) / 2)
	bottomPad = round(float(shape2[1] - shape[0]) - topPad)
	pads = ((leftPad,rightPad),(topPad,bottomPad))

	for i,x in enumerate(image_array):
		x_p = np.pad(x, pads, 'constant', constant_values=(0))
		img_arr[i,:,:] = x_p

	return np.array(img_arr).transpose(1, 2, 0)

def process_pooling(file_name, filter_size, stride):
	filter_size = int(filter_size)
	stride = int(stride)

	start = dt.now()
	image_array = read_file(file_name)
	image_array = normalize(image_array)
	image_array = max_pooling(image_array, filter_size, stride)
	image_array = rescale(image_array, 0, 255) # valid range to save image [0, 255]
	new_file_name = file_name_no_ext(file_name) + "_pool" + ".jpg"
	save_image(image_array, new_file_name)
	print("File " + new_file_name + " created")

	end = dt.now()
	delta = end - start
	total = round(delta.seconds + delta.microseconds/1E6, 2) # from http://stackoverflow.com/a/2880735/1065981

	print("Max pooling: " + str(total) + " seconds")

def process_convolution(file_name, filters_number, filter_size, stride, pad=0, convs_number=1):
	filter_size = int(filter_size)
	convs_number = int(convs_number)
	stride = int(stride)
	pad = int(pad)
	filters_number = int(filters_number)

	if convs_number > 6: # 6 convs at most, to avoid to much computation
		convs_number = 6
	if convs_number < 1: # at least one conv
		convs_number = 1

	if filters_number < 3: # at least 3 filters for RGB purposes
		filters_number = 3

	image_array = read_file(file_name)
	image_array = normalize(image_array)

	for c in range(1, convs_number + 1):

		start = dt.now()
		image_array = padding(image_array, pad)
		image_array = convolute(image_array, filters_number, filter_size, stride)

		if image_array is None:
			print("Stopped. Can't create more convolutions")
			return
		
		image_array = rescale(image_array, 0, 255) # valid range to save image [0, 255]
		image_array = image_array[:,:,:3]
		new_file_name = file_name_no_ext(file_name) + "_conv" + str(c) + ".jpg"
		save_image(image_array, new_file_name)
		print("File " + new_file_name + " created")

		image_array = rescale(image_array, 0, 1) # rescale to be a valid RGB value [0,  1] for next conv
		
		end = dt.now()
		delta = end - start
		total = round(delta.seconds + delta.microseconds/1E6, 2) # from http://stackoverflow.com/a/2880735/1065981

		print("Convolution " + str(c) + ": " + str(total) + " seconds")

parser = argparse.ArgumentParser(description='Process an image.')
parser.add_argument('file_name', type=str, help="File name of the image (jpg only)")
parser.add_argument('-o', dest='operation', type=str, default='max_pooling', help="Operation to perform in the image (convolution, max_pooling)")
parser.add_argument('-f', dest='filter_size', type=int, default=4)
parser.add_argument('-s', dest='stride', type=int, default=1)
parser.add_argument('-p', dest='padding', type=int, default=1)
parser.add_argument('-n', dest='number_convolutions', type=int, default=1)
parser.add_argument('-u', dest='number_filters', type=int, default=1)
# parser.add_argument('integers', metavar='N', type=int, nargs='+',
#                     help='an integer for the accumulator')
# parser.add_argument('--sum', dest='accumulate', action='store_const',
#                     const=sum, default=max,
#                     help='sum the integers (default: find the max)')

args = parser.parse_args()
file_name = args.file_name
operation = args.operation

if operation == "max_pooling":
	process_pooling(file_name, args.filter_size, args.stride)
elif operation == "convolution":
	process_convolution(file_name, args.number_filters, args.filter_size, args.stride, args.padding, args.number_convolutions)
else:
	print("Invalid operation: " + operation)