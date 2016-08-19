# Remove background
Final project of Machine Learning Nanodegree program.

It classifies the pixels in an image either as background or as foreground, based on defined spots.

## Requirements
Python 2.7, PyQt4, Numpy, Sklearn, Skimage, Scipy, Matplotlib.

This project has been tested under Ubuntu 14.04 32 bits.

## Installation
Download and unzip the content of the project to your selected location. Alternatively, you can clone the repository.

## How to use it
In a terminal, go to the localion of the contents and type: **python main.py**

If all the requirements are met, the next window will be shown:


![alt tag](http://i1041.photobucket.com/albums/b414/sosegon/Screenshot-MainWindow-1.png)

1. Click on "Open Image" and select an image in JPG format.
2. Define the background and foreground spots to classify the pixels:
 * Click on the corresponding button: "Set Background" or "Set Foreground"
 * Define the area by dragging and dropping the mouse pointer over the left canvas

3. If a benchmark is provided, click on "Benchmark" and select the corresponding image.
4. Click on "Process" and wait until the application finishes.

The result is shown in the right canvas.

Images of every process are saved in the folder where the image was selected.

Next images will be generated:
 * file_name_classified.jpg
 * file_name_convexHull.jpg
 * file_name_spotless.jpg

Plots and a log for further analysis are saved in the folder where the application is executed.

Next files will be created:
 * Red_component_in_Foreground_histo.png
 * Red_component_in_Foreground.png
 * Green_component_in_Foreground_histo.png
 * Green_component_in_Foreground.png
 * Blue_component_in_Foreground_histo.png
 * Blue_component_in_Foreground.png
 * Red_component_in_Background_histo.png
 * Red_component_in_Background.png
 * Green_component_in_Background_histo.png
 * Green_component_in_Background.png
 * Blue_component_in_Background_histo.png
 * Blue_component_in_Background.png
 * scene_result.jpg
 * scene_with_regions.jpg
 * processing_log.txt

## License
The MIT License





