import sys
import pyqtgraph as pg
from pyqtgraph.Qt import QtCore, QtGui
import numpy as np

class  CapImageView(pg.GraphicsView):
    pg.setConfigOption('background', 'w')
    pg.setConfigOption('foreground', 'k')
    ptr1 = 0
    def __init__(self, parent=None, **kargs):
        pg.GraphicsView.__init__(self, **kargs)
        self.setParent(parent)
        self.setWindowTitle('Cap Image View')
        frame = np.random.normal(size=(100, 100))
        self.img_view = pg.ImageView()
        self.img_view.setImage(frame)
        self.add(self.img_view)

    def set_image(self, im):
        self.img_view.set_image(im)
        print("received new image")

