# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'MainWindow.ui'
#
# Created by: PyQt5 UI code generator 5.11.3
#
# WARNING! All changes made in this file will be lost!

from PyQt5 import QtCore, QtGui, QtWidgets

class Ui_mainWindow(object):
    def setupUi(self, mainWindow):
        mainWindow.setObjectName("mainWindow")
        mainWindow.resize(433, 433)
        self.centralwidget = QtWidgets.QWidget(mainWindow)
        self.centralwidget.setObjectName("centralwidget")
        self.verticalLayout_2 = QtWidgets.QVBoxLayout(self.centralwidget)
        self.verticalLayout_2.setObjectName("verticalLayout_2")
        self.horizontalLayout = QtWidgets.QHBoxLayout()
        self.horizontalLayout.setObjectName("horizontalLayout")
        self.CapImage = CapImageView(self.centralwidget)
        sizePolicy = QtWidgets.QSizePolicy(QtWidgets.QSizePolicy.MinimumExpanding, QtWidgets.QSizePolicy.Preferred)
        sizePolicy.setHorizontalStretch(0)
        sizePolicy.setVerticalStretch(0)
        sizePolicy.setHeightForWidth(self.CapImage.sizePolicy().hasHeightForWidth())
        self.CapImage.setSizePolicy(sizePolicy)
        self.CapImage.setObjectName("CapImage")
        self.horizontalLayout.addWidget(self.CapImage)
        self.verticalLayout = QtWidgets.QVBoxLayout()
        self.verticalLayout.setSpacing(4)
        self.verticalLayout.setObjectName("verticalLayout")
        self.revert_btn = QtWidgets.QPushButton(self.centralwidget)
        self.revert_btn.setMaximumSize(QtCore.QSize(80, 16777215))
        self.revert_btn.setObjectName("revert_btn")
        self.verticalLayout.addWidget(self.revert_btn)
        spacerItem = QtWidgets.QSpacerItem(20, 40, QtWidgets.QSizePolicy.Minimum, QtWidgets.QSizePolicy.Expanding)
        self.verticalLayout.addItem(spacerItem)
        self.next_btn = QtWidgets.QPushButton(self.centralwidget)
        self.next_btn.setMaximumSize(QtCore.QSize(80, 16777215))
        self.next_btn.setObjectName("next_btn")
        self.verticalLayout.addWidget(self.next_btn)
        self.horizontalLayout.addLayout(self.verticalLayout)
        self.verticalLayout_2.addLayout(self.horizontalLayout)
        self.horizontalLayout_2 = QtWidgets.QHBoxLayout()
        self.horizontalLayout_2.setObjectName("horizontalLayout_2")
        self.label = QtWidgets.QLabel(self.centralwidget)
        self.label.setObjectName("label")
        self.horizontalLayout_2.addWidget(self.label)
        self.id_lbl = QtWidgets.QLabel(self.centralwidget)
        self.id_lbl.setObjectName("id_lbl")
        self.horizontalLayout_2.addWidget(self.id_lbl)
        spacerItem1 = QtWidgets.QSpacerItem(20, 20, QtWidgets.QSizePolicy.Maximum, QtWidgets.QSizePolicy.Minimum)
        self.horizontalLayout_2.addItem(spacerItem1)
        self.label_3 = QtWidgets.QLabel(self.centralwidget)
        self.label_3.setObjectName("label_3")
        self.horizontalLayout_2.addWidget(self.label_3)
        self.input_lbl = QtWidgets.QLabel(self.centralwidget)
        self.input_lbl.setObjectName("input_lbl")
        self.horizontalLayout_2.addWidget(self.input_lbl)
        spacerItem2 = QtWidgets.QSpacerItem(20, 20, QtWidgets.QSizePolicy.Maximum, QtWidgets.QSizePolicy.Minimum)
        self.horizontalLayout_2.addItem(spacerItem2)
        self.label_5 = QtWidgets.QLabel(self.centralwidget)
        self.label_5.setObjectName("label_5")
        self.horizontalLayout_2.addWidget(self.label_5)
        self.progress_lbl = QtWidgets.QLabel(self.centralwidget)
        self.progress_lbl.setObjectName("progress_lbl")
        self.horizontalLayout_2.addWidget(self.progress_lbl)
        spacerItem3 = QtWidgets.QSpacerItem(40, 20, QtWidgets.QSizePolicy.Expanding, QtWidgets.QSizePolicy.Minimum)
        self.horizontalLayout_2.addItem(spacerItem3)
        self.verticalLayout_2.addLayout(self.horizontalLayout_2)
        mainWindow.setCentralWidget(self.centralwidget)

        self.retranslateUi(mainWindow)
        QtCore.QMetaObject.connectSlotsByName(mainWindow)

    def retranslateUi(self, mainWindow):
        _translate = QtCore.QCoreApplication.translate
        mainWindow.setWindowTitle(_translate("mainWindow", "Cap Image Viewer"))
        self.revert_btn.setText(_translate("mainWindow", "Revert"))
        self.next_btn.setText(_translate("mainWindow", "Next"))
        self.label.setText(_translate("mainWindow", "ID:"))
        self.id_lbl.setText(_translate("mainWindow", "UserId"))
        self.label_3.setText(_translate("mainWindow", "CurrentInput:"))
        self.input_lbl.setText(_translate("mainWindow", "Input"))
        self.label_5.setText(_translate("mainWindow", "Progress:"))
        self.progress_lbl.setText(_translate("mainWindow", "1/1234"))

from capimageview import CapImageView
