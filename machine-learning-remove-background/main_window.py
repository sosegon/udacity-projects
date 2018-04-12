# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'main_window.ui'
#
# Created by: PyQt4 UI code generator 4.11.4
#
# WARNING! All changes made in this file will be lost!

from PyQt4 import QtCore, QtGui

try:
    _fromUtf8 = QtCore.QString.fromUtf8
except AttributeError:
    def _fromUtf8(s):
        return s

try:
    _encoding = QtGui.QApplication.UnicodeUTF8
    def _translate(context, text, disambig):
        return QtGui.QApplication.translate(context, text, disambig, _encoding)
except AttributeError:
    def _translate(context, text, disambig):
        return QtGui.QApplication.translate(context, text, disambig)

class Ui_MainWindow(object):
    def setupUi(self, MainWindow):
        MainWindow.setObjectName(_fromUtf8("MainWindow"))
        MainWindow.resize(1005, 559)
        self.centralwidget = QtGui.QWidget(MainWindow)
        self.centralwidget.setObjectName(_fromUtf8("centralwidget"))
        self.horizontalLayoutWidget = QtGui.QWidget(self.centralwidget)
        self.horizontalLayoutWidget.setGeometry(QtCore.QRect(0, 0, 680, 41))
        self.horizontalLayoutWidget.setObjectName(_fromUtf8("horizontalLayoutWidget"))
        self.horizontalLayout = QtGui.QHBoxLayout(self.horizontalLayoutWidget)
        self.horizontalLayout.setObjectName(_fromUtf8("horizontalLayout"))
        self.btn_open_image = QtGui.QPushButton(self.horizontalLayoutWidget)
        self.btn_open_image.setObjectName(_fromUtf8("btn_open_image"))
        self.horizontalLayout.addWidget(self.btn_open_image)
        self.btn_benchmark = QtGui.QPushButton(self.horizontalLayoutWidget)
        self.btn_benchmark.setObjectName(_fromUtf8("btn_benchmark"))
        self.horizontalLayout.addWidget(self.btn_benchmark)
        self.btn_set_foreground = QtGui.QPushButton(self.horizontalLayoutWidget)
        self.btn_set_foreground.setObjectName(_fromUtf8("btn_set_foreground"))
        self.horizontalLayout.addWidget(self.btn_set_foreground)
        self.btn_set_background = QtGui.QPushButton(self.horizontalLayoutWidget)
        self.btn_set_background.setObjectName(_fromUtf8("btn_set_background"))
        self.horizontalLayout.addWidget(self.btn_set_background)
        self.btn_reset_areas = QtGui.QPushButton(self.horizontalLayoutWidget)
        self.btn_reset_areas.setObjectName(_fromUtf8("btn_reset_areas"))
        self.horizontalLayout.addWidget(self.btn_reset_areas)
        self.btn_process = QtGui.QPushButton(self.horizontalLayoutWidget)
        self.btn_process.setObjectName(_fromUtf8("btn_process"))
        self.horizontalLayout.addWidget(self.btn_process)
        self.splitter = QtGui.QSplitter(self.centralwidget)
        self.splitter.setGeometry(QtCore.QRect(1, 41, 1001, 421))
        self.splitter.setOrientation(QtCore.Qt.Horizontal)
        self.splitter.setObjectName(_fromUtf8("splitter"))
        self.grv_image = QtGui.QGraphicsView(self.splitter)
        self.grv_image.setObjectName(_fromUtf8("grv_image"))
        self.grv_image_result = QtGui.QGraphicsView(self.splitter)
        self.grv_image_result.setObjectName(_fromUtf8("grv_image_result"))
        self.lbl_score = QtGui.QLabel(self.centralwidget)
        self.lbl_score.setGeometry(QtCore.QRect(0, 460, 361, 51))
        self.lbl_score.setText(_fromUtf8(""))
        self.lbl_score.setObjectName(_fromUtf8("lbl_score"))
        self.lbl_benchmark = QtGui.QLabel(self.centralwidget)
        self.lbl_benchmark.setGeometry(QtCore.QRect(510, 460, 361, 51))
        self.lbl_benchmark.setText(_fromUtf8(""))
        self.lbl_benchmark.setObjectName(_fromUtf8("lbl_benchmark"))
        self.lbl_score.raise_()
        self.horizontalLayoutWidget.raise_()
        self.splitter.raise_()
        self.lbl_benchmark.raise_()
        MainWindow.setCentralWidget(self.centralwidget)
        self.menubar = QtGui.QMenuBar(MainWindow)
        self.menubar.setGeometry(QtCore.QRect(0, 0, 1005, 25))
        self.menubar.setObjectName(_fromUtf8("menubar"))
        MainWindow.setMenuBar(self.menubar)
        self.statusbar = QtGui.QStatusBar(MainWindow)
        self.statusbar.setObjectName(_fromUtf8("statusbar"))
        MainWindow.setStatusBar(self.statusbar)

        self.retranslateUi(MainWindow)
        QtCore.QMetaObject.connectSlotsByName(MainWindow)

    def retranslateUi(self, MainWindow):
        MainWindow.setWindowTitle(_translate("MainWindow", "MainWindow", None))
        self.btn_open_image.setText(_translate("MainWindow", "Open Image", None))
        self.btn_benchmark.setText(_translate("MainWindow", "Benchmark", None))
        self.btn_set_foreground.setText(_translate("MainWindow", "Set ForeGround", None))
        self.btn_set_background.setText(_translate("MainWindow", "Set Background", None))
        self.btn_reset_areas.setText(_translate("MainWindow", "Reset areas", None))
        self.btn_process.setText(_translate("MainWindow", "Process", None))

