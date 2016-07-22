import os
import sys
import main_window
import numpy as np

from PyQt4 import QtGui, QtCore, Qt

from scipy import ndimage as ndi
from time import time

from skimage import io
from skimage.color import rgb2gray
from skimage.morphology import convex_hull_object

from sklearn import tree
from sklearn import grid_search
from sklearn.metrics import accuracy_score as the_scorer
from sklearn.metrics import make_scorer
from sklearn.cross_validation import train_test_split

import matplotlib.pyplot as plt
import matplotlib.mlab as mlab

class ImageProcessingApp(QtGui.QMainWindow, main_window.Ui_MainWindow):

    def __init__(self):
        
        super(self.__class__, self).__init__()
        self.setupUi(self)  

        self.scene = OriginalGraphicsScene(self, self.statusbar)       # For original image
        self.scene_result = ProcessedGraphicsScene(self)               # For processed image
        self.image_data_array = None                                   # To do the classification
        self.bench_data_array = None                                   # Benchmark
        self.image_file_name = None
        self.image_file_name_no_ext = None
        self.log_text = ""
        
        # Listeners for buttons
        self.btn_benchmark.clicked.connect(self.open_benchmark)
        self.btn_open_image.clicked.connect(self.open_image)
        self.btn_set_background.clicked.connect(self.set_background_region)
        self.btn_set_foreground.clicked.connect(self.set_foreground_region)
        self.btn_reset_areas.clicked.connect(self.reset_areas)
        self.btn_process.clicked.connect(self.process)

    def open_benchmark(self):
        command = 'Open image'
        path = os.path.dirname(os.path.abspath(__file__))               # where the script is being executed
        file_types = "Image files (*.jpg)"                              # Just jpg images to get just the R, G and B channels
        file_name = QtGui.QFileDialog.getOpenFileName(self, command, path, file_types)

        if str(file_name) is not '':
            self.bench_data_array = io.imread(str(file_name))
            self.statusbar.showMessage(QtCore.QString.fromUtf8("Benchmark: " + str(file_name)))
            self.log_text = self.log_text + "\nBenchmark: " + str(file_name)
        else:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("No benchmark opened"))
            self.log_text = self.log_text + "\nBenchmark: " + "Not provided"

    def open_image(self):
        self.scene.reset()
        self.scene_result.reset()

        command = 'Open image'
        path = os.path.dirname(os.path.abspath(__file__))               # where the script is being executed
        file_types = "Image files (*.jpg)"                              # Just jpg images to get just the R, G and B channels
        file_name = QtGui.QFileDialog.getOpenFileName(self, command, path, file_types)    

        if str(file_name) is not '':
            self.log_text = ""
            pxm_image = QtGui.QPixmap(file_name)

            self.scene.addPixmap(pxm_image)
            self.grv_image.setScene(self.scene)

            self.image_file_name = str(file_name)
            self.image_data_array = io.imread(self.image_file_name)
            r = self.image_file_name[::-1]                                  # reverse name
            n = r[r.index(".") + 1:]                                        # reverse name extensionless
            self.image_file_name_no_ext = n[::-1]
            self.statusbar.showMessage(QtCore.QString.fromUtf8("Image: " + self.image_file_name))
            self.log_text = self.log_text + "\nFile name: " + self.image_file_name
        else:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("No image opened"))

    def set_background_region(self):
        self.scene.setRegionToAdd(-1)
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Defining background spots"))

    def set_foreground_region(self):
        self.scene.setRegionToAdd(1)
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Defining foreground spots"))

    def reset_areas(self):
        self.scene.remove_regions()
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Areas removed"))

    def process(self):
        if self.image_data_array is None:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("No image provided"))
            return

        if len(self.scene.background_regions) == 0:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("No background spots defined"))
            return

        if len(self.scene.foreground_regions) == 0:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("No foreground spots defined"))
            return

        self.save_scene(self.scene, "scene_with_regions.jpg")           # Image used in the document
        dimens = self.image_data_array.shape
        self.log_text = self.log_text + "\nImage size: {} x {}".format(dimens[1], dimens[0])

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Preparing: Setting input for model"))
        features, target = self.create_features_and_labels()

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Preparing: Splitting data for training and testing"))
        n_test_points = int(round(target.size * .3))                # 30% for testing
        X_train, X_test, y_train, y_test = train_test_split(features, target, test_size=n_test_points, random_state=10, stratify=target)

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Preparing: Training model"))
        classif = tree.DecisionTreeClassifier()
        parameters = {'max_depth':(1,5,10), 'min_samples_leaf':(1,5,10)}
        scoring_function = make_scorer(the_scorer, greater_is_better=True)
        classifier = grid_search.GridSearchCV(classif, parameters, scoring_function)
        classifier.fit(X_train, y_train)

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Preparing: Predicting training set"))
        y_pred = classifier.predict(X_train)
        scorer_text = "Accuracy scorer \nTrainig: {:.4f}".format(the_scorer(y_train, y_pred))

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Preparing: Predicting testing set"))
        y_pred = classifier.predict(X_test)
        scorer_text = scorer_text + "\nTesting: {:.4f}".format(the_scorer(y_test, y_pred))
        self.lbl_score.setText(QtCore.QString.fromUtf8(scorer_text))
        self.log_text = self.log_text + "\n" + scorer_text

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Processing: classifying pixels"))
        start = time()
        segmented_image = self.classify_pixels(classifier)
        end = time()
        self.save_array_pixels_as_image(segmented_image, self.image_file_name_no_ext + "_classified.jpg")
        self.log_text = self.log_text + "\nTime to classify image {:.1f} seconds".format(end - start)
        self.log_text = self.log_text + "\nClassified image: " + self.image_file_name_no_ext + "_classified.jpg"

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Processing: convex hull"))
        start = time()
        convex_hull_image = self.convex_hull(segmented_image)
        end = time()
        self.save_array_pixels_as_image(convex_hull_image, self.image_file_name_no_ext + "_convexHull.jpg")
        self.log_text = self.log_text + "\nTime to convex hull image {:.1f} seconds".format(end - start)
        self.log_text = self.log_text + "\nConvex hull image: " + self.image_file_name_no_ext + "_convexHull.jpg"

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Processing: removing small spots"))
        start = time()
        spotless_image = self.remove_spots(convex_hull_image)
        end = time()
        self.save_array_pixels_as_image(spotless_image, self.image_file_name_no_ext + "_spotless.jpg")
        self.log_text = self.log_text + "\nTime to remove spots {:.1f} seconds".format(end - start)
        self.log_text = self.log_text + "\nSpotless image: " + self.image_file_name_no_ext + "_spotless.jpg"

        ################################################################################################################################
        if self.bench_data_array is not None:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("Postprocessing: benchmark comparison"))
            self.benchmark_comparison(spotless_image)

        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Postprocessing: displaying result"))
        self.display_result(spotless_image)
        
        ################################################################################################################################
        self.statusbar.showMessage(QtCore.QString.fromUtf8("Done"))

        with open('processing_log.txt', 'w') as mylog:
            mylog.write(self.log_text)

    def display_result(self, pixels_array):
        h, w, bpl = pixels_array.shape
        qimage = QtGui.QImage(pixels_array.data, w, h, bpl * w, QtGui.QImage.Format_RGB888)

        pxm_image = QtGui.QPixmap.fromImage(qimage)
        self.scene_result.reset()
        self.scene_result.addPixmap(pxm_image)
        self.grv_image_result.setScene(self.scene_result)
        self.save_scene(self.scene_result, "scene_result.jpg")                  # Image used in the document

    def benchmark_comparison(self, pixels_array):
        if self.bench_data_array.shape != pixels_array.shape:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("Postprocessing: Not possible to compare with benchmark"))
            return

        real = self.bench_data_array.ravel()
        predicted = pixels_array.ravel()

        accuracy = the_scorer(real, predicted)

        self.lbl_benchmark.setText(QtCore.QString.fromUtf8("The accuracy to the benchmark is {:.4f}".format(accuracy)))
        self.log_text = self.log_text + "\nThe accuracy to the benchmark is {:.4f}".format(accuracy)


    def get_pixel_components_as_features(self, coords):
        regions = np.array([[0,0,0]])
        for coord in coords:
            n_region = self.image_data_array[coord[1]:coord[3], coord[0]:coord[2], :]
            n_region_reshaped = np.reshape(n_region, (-1, 3))
            regions = np.concatenate((regions, n_region_reshaped))

        regions = np.delete(regions, 0, 0) # remove first element, unuseful, juts for concatenation purpose

        return regions

    def plot_feature(self, feature, spot, title, x_label, y_label):
        shape = feature.shape
        rang = np.arange(0, shape[0])

        variance = np.var(feature)
        mean = round(np.mean(feature))
        maxi = np.max(feature)
        mini = np.min(feature)

        # scattered plot
        plt.clf()
        plt.plot(rang, feature, spot)
        plt.axis([0, shape[0], 0, 255])
        plt.title(title + "\n (variance: {:.1f}) (range: [{}, {}]) (mean: {:.0f})".format(variance, mini, maxi, mean))
        plt.xlabel(x_label)
        plt.ylabel(y_label + " values")
        plt.savefig(title.replace(" ", "_"))

        # histogram
        plt.clf()
        mu, sigma = mean, np.std(feature)
        n, bins, patches = plt.hist(feature, 25, normed=1, facecolor=y_label.lower(), alpha=0.75)
        y = mlab.normpdf(bins, mu, sigma)
        l = plt.plot(bins, y, 'y--', linewidth=1)
        plt.xlabel(y_label + " values")
        plt.ylabel('Probability')
        plt.title("Histogram of {}".format(title) + "\n" + r'$\mu={:.0f},\ \sigma={:.1f}$'.format(mu, sigma))
        plt.savefig(title.replace(" ", "_") + "_histo")

    def visualize_features(self, b_features, f_features):

        features = [
            [b_features[:,0], 'ro', "Red component in Background", "Pixels", "Red"],
            [b_features[:,1], 'bo', "Blue component in Background", "Pixels", "Blue"],
            [b_features[:,2], 'go', "Green component in Background", "Pixels", "Green"],
            [f_features[:,0], 'r+', "Red component in Foreground", "Pixels", "Red"],
            [f_features[:,1], 'b+', "Blue component in Foreground", "Pixels", "Blue"],
            [f_features[:,2], 'g+', "Green component in Foreground", "Pixels", "Green"]
        ]

        for feat, spot, title, x_label, y_label in features:
            self.plot_feature(feat, spot, title, x_label, y_label)

    def save_scene(self, scene, name):
        view = QtGui.QGraphicsView(scene)
        pixmap = QtGui.QPixmap.grabWidget(view)
        pixmap.save(name)

    def create_features_and_labels(self):
        background_coords = self.scene.get_region_coords(-1)
        self.log_text = self.log_text + "\nBackground spots defined: " + str(len(background_coords))
        background_features = self.get_pixel_components_as_features(background_coords)
        background_labels = np.ones(background_features.shape[0])
        background_labels.fill(-1)

        foreground_coords = self.scene.get_region_coords(1)
        self.log_text = self.log_text + "\nForeground spots defined: " + str(len(foreground_coords))
        foreground_features = self.get_pixel_components_as_features(foreground_coords)
        foreground_labels = np.ones(foreground_features.shape[0])

        self.visualize_features(background_features, foreground_features)

        features = np.concatenate((background_features, foreground_features))
        target = np.concatenate((background_labels, foreground_labels))

        self.log_text = self.log_text + "\nPixels used in the model: " + str(len(target))

        return features, target

    def save_array_pixels_as_image(self, pixels_array, file_name):
        io.imsave(file_name, pixels_array)
    
    def classify_pixels(self, classifier):
        image_vector_pixels = np.copy(self.image_data_array)
        image_vector_pixels = np.reshape(image_vector_pixels, (-1, 3));        # shape is (#rows x #cols, 3)
        y_pred = classifier.predict(image_vector_pixels)                       # shape is (#rows x #cols)

        it = np.nditer(y_pred, flags=['f_index'])                              # iterate image to process pixels based on their class
        while not it.finished:
            if it[0] == -1:
                image_vector_pixels[it.index] = [0, 0, 0]                      # pixels in background set to black
            it.iternext()

        image_array_pixels = np.reshape(image_vector_pixels, self.image_data_array.shape) #shape is (#rows, #cols, 3)

        return image_array_pixels

    def convex_hull(self, pixels_array):
        image_binary = self.image_as_binary(pixels_array)

        blacks = convex_hull_object(image_binary)
        blacks = np.invert(blacks)

        copy_image = np.copy(self.image_data_array)
        copy_image[blacks] = [0, 0, 0]

        return copy_image

    def remove_spots(self, pixels_array):
        image_binary = rgb2gray(pixels_array)                       # grayscale, shape is (#rows, #cols)
        whites = image_binary > 0
        image_binary[whites] = 1                                    # binary image 1 or 0
        
        label_objects, nb_labels = ndi.label(image_binary)
        sizes = np.bincount(label_objects.ravel())

        mask_sizes = sizes > 25
        mask_sizes[0] = 0
        image_binary = mask_sizes[label_objects]

        whites = image_binary > 0
        blacks = np.invert(whites)

        copy_image = np.copy(self.image_data_array)
        copy_image[blacks] = [0, 0, 0]

        return copy_image

    def remove_spots_2(self, pixels_array):
        from skimage.morphology import disk
        from skimage.morphology import closing

        image_binary = self.image_as_binary(pixels_array)
        image_binary = self.invert_binary(image_binary)
        
        selem = disk(25)
        image_binary = closing(image_binary, selem)
        image_binary = self.invert_binary(image_binary)

        whites = image_binary > 0
        blacks = np.invert(whites)

        copy_image = np.copy(self.image_data_array)
        copy_image[blacks] = [0, 0, 0]

        return copy_image

    def image_as_binary(self, pixels_array):
        image_binary = rgb2gray(pixels_array)                       # grayscale, shape is (#rows, #cols)
        whites = image_binary > 0
        image_binary[whites] = 1                                    # binary image 1 or 0

        return image_binary

    def invert_binary(self, pixels_binary):
        shape = pixels_binary.shape

        pixels_vector = np.reshape(pixels_binary, -1)
        bools_vector = pixels_vector.astype(bool)
        inv_bools_vector = np.invert(bools_vector)

        pixels_vector = inv_bools_vector.astype(int)
        pixels_array = np.reshape(pixels_vector, shape)

        return pixels_array

   
class ProcessedGraphicsScene(QtGui.QGraphicsScene):
    def __init__(self, parent):
        QtGui.QGraphicsScene.__init__(self, parent)
        self.reset()                   

    def reset(self):
        while len(self.items()) > 0:
            item = self.items()[-1]
            self.removeItem(item)


class OriginalGraphicsScene(QtGui.QGraphicsScene):
    def __init__(self, parent, statusbar):
        QtGui.QGraphicsScene.__init__(self, parent)
        self.reset()
        self.statusbar = statusbar

    def remove_regions(self):
        self.remove_background_regions()
        self.remove_foreground_regions()

    def remove_background_regions(self):
        for region in self.background_regions:
            self.removeItem(region)

        self.background_regions = []

    def remove_foreground_regions(self):
        for region in self.foreground_regions:
            self.removeItem(region)

        self.foreground_regions = []
    
    def reset(self):
        self.background_regions = []
        self.foreground_regions = []
        self.region_to_add = 0                      # 0: None, 1: Foreground, -1: Background
        self.last_region_origin = []
        self.last_region_added = None

        while len(self.items()) > 0:
            item = self.items()[-1]
            self.removeItem(item)

    def get_region_coords(self, bid):
        coords = []
        regions = None
        if bid == 1:
            regions = self.foreground_regions
        elif bid == -1:
            regions = self.background_regions
        else:
            return coords

        for rect_item in regions:
            rect = rect_item.rect()
            coord = (rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height()) #(col, row, col, row)
            coords.append(coord)

        return coords

    def setRegionToAdd(self, value):
        self.region_to_add = value

    def mousePressEvent(self, event):
        color = None
        regions = None
        r, g, b, a = 0, 0, 0, 64
        if self.region_to_add == 1:                 # Create a new foreground area
            g = 255
            regions = self.foreground_regions
            self.statusbar.showMessage(QtCore.QString.fromUtf8("Defining foreground spots"))
        elif self.region_to_add == -1:              # Create a new background area
            r = 255
            regions = self.background_regions
            self.statusbar.showMessage(QtCore.QString.fromUtf8("Defining background spots"))
        else:
            self.statusbar.showMessage(QtCore.QString.fromUtf8("No region to add"))
            return

        rect = self.createRect(event.scenePos())
        color = QtGui.QColor(r, g, b, a)
        brush = QtGui.QBrush(color)
        pen = QtGui.QPen()
        pen.setWidth(1)

        self.last_region_added = self.addRect(rect, pen, brush)
        regions.append(self.last_region_added)
        self.last_region_origin = [event.scenePos().x(), event.scenePos().y()]

    def mouseMoveEvent(self, event):                #Resize background area
        rectItem = None

        if self.region_to_add == 1:
            rectItem = self.foreground_regions[-1]
        elif self.region_to_add == -1:
            rectItem = self.background_regions[-1]
        else:
            return

        rect = rectItem.rect()
        pos_x = event.scenePos().x()
        pos_y = event.scenePos().y()

        if pos_x < self.last_region_origin[0]:
            ori_x = pos_x
            width = self.last_region_origin[0] - pos_x
        else:
            ori_x = rect.x()
            width = pos_x - rect.x()

        if pos_y < self.last_region_origin[1]:
            ori_y = pos_y
            height = self.last_region_origin[1] - pos_y
        else:
            ori_y = rect.y()
            height = pos_y - rect.y()

        rectItem.setRect(ori_x, ori_y, width, height)

    def mouseReleaseEvent(self, event):
        if self.last_region_added is not None:
            self.checkLastRectInRegions(self.background_regions)

        if self.last_region_added is not None:
            self.checkLastRectInRegions(self.foreground_regions)

    def checkLastRectInRegions(self, regions):
        for region in regions:
            overlapped = self.overlapped(region, self.last_region_added)
            if region is not self.last_region_added and overlapped is True:
                self.statusbar.showMessage(QtCore.QString.fromUtf8("No overlapping spots allowed"))
                if self.last_region_added in self.background_regions:
                    self.background_regions.remove(self.last_region_added)
                elif self.last_region_added in self.foreground_regions:
                    self.foreground_regions.remove(self.last_region_added)
                self.removeItem(self.last_region_added)
                self.last_region_added = None
                break

    def createRect(self, start_point):  
        size = QtCore.QSizeF(0, 0)
        rect = QtCore.QRectF(start_point, size)

        return rect

    def overlapped(self, rectItem1, rectItem2):
        rect1 = rectItem1.rect()
        rect2 = rectItem2.rect()

        x1_min = rect1.x()
        x1_max = rect1.width() + x1_min
        y1_min = rect1.y()
        y1_max = rect1.height() + y1_min

        x2_min = rect2.x()
        x2_max = rect2.width() + x2_min
        y2_min = rect2.y()
        y2_max = rect2.height() + y2_min

        dx = min(x1_max, x2_max) - max(x1_min, x2_min)
        dy = min(y1_max, y2_max) - max(y1_min, y2_min)

        return dx >= 0 and dy >= 0


def main():
    app = QtGui.QApplication(sys.argv)  # A new instance of QApplication
    form = ImageProcessingApp()                 # We set the form to be our ExampleApp (design)
    form.show()                         # Show the form
    sys.exit(app.exec_())
    #app.exec_()                         # and execute the app

if __name__ == '__main__':              # if we're running file directly and not importing it
    main()              