import matplotlib.pyplot as plt 
import matplotlib.image as mpimg
import numpy as np 

image = mpimg.imread('white_filter_test.jpg')

shape = image.shape

print "image size is {:.0f} x {:.0f}".format(shape[1], shape[0])

xdim = shape[1]
ydim = shape[0]

# thresholds to filter the white pixels
ths_rgb = [200, 200, 200]

colors_thresholds = (image[:,:,0] < ths_rgb[0]) | (image[:,:,1] < ths_rgb[1]) | (image[:,:,2] < ths_rgb[2])

colors_select = np.copy(image)

colors_select[colors_thresholds] = [0, 0, 0] # make black pixels below the threshold

#plt.imshow(colors_select)
plt.show()

# Create a polygon (triangle) mask to just get the region of interest

# Define the three points, bottom left, bottom right, and apex

b_left = [0, ydim - 1]
b_right = [xdim - 1, ydim - 1]
apex = [xdim / 2, ydim / 2]

# Those three points creates the lines of the triangle
# y = mx + b we need the polynomial coefficients

coef_left = np.polyfit((b_left[0], apex[0]), (b_left[1], apex[1]), 1)
coef_right = np.polyfit((b_right[0], apex[0]), (b_right[1], apex[1]), 1)
coef_bottom = np.polyfit((b_left[0], b_right[0]), (b_left[1], b_right[1]), 1)


# Find the region inside the lines
# Next are vectors of a cartesina plane
XX, YY = np.meshgrid(np.arange(0, xdim), np.arange(0, ydim))

# Use the line definitions (coefficients) to determine the pixels
# within the triangle
region_thresholds = (YY > (XX*coef_left[0] + coef_left[1])) & \
                    (YY > (XX*coef_right[0] + coef_right[1])) & \
                    (YY < (XX*coef_bottom[0] + coef_bottom[1]))

region_select = np.copy(image)
region_select[region_thresholds] = [255, 0, 0] # make the region red

#plt.imshow(region_select)
plt.show()

region_colors_select = np.copy(image)
# Make black pixels outside the region or below the color thresholds
region_colors_select[colors_thresholds | ~region_thresholds] = [0, 0, 0] 
# Make red pixels within the region and above the color thresholds
region_colors_select[~colors_thresholds & region_thresholds] = [255, 0, 0] 

plt.imshow(region_colors_select)
plt.show()
