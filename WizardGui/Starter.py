from capimageview import CapImageView
from PyQt5 import QtCore, QtGui, QtWidgets
from MainWindow import Ui_mainWindow
from PyQt5.QtCore import QThread, QObject, pyqtSignal
import socket
import pyqtgraph as pg
import numpy as np


class Starter(QObject):

    PHONE_IP = "192.168.0.102"
    PHONE_PORT = 1234
    cap_sig = pyqtSignal(str)

    def __init__(self):
        super().__init__()
        self.app = QtWidgets.QApplication(sys.argv)
        self.MainWindow = QtWidgets.QMainWindow()
        self.ui = Ui_mainWindow()
        self.ui.setupUi(self.MainWindow)
        self.__init_values()
        self.MainWindow.show()
        sys.exit(self.app.exec_())

    def __received_data(self, data):
        try:
            img = np.array(data.split(";")[-1].split(",")[2:-1], dtype=np.float64)
            img[img < 0] = 0
            img = img.reshape(27, 15).transpose()
            self.img_view.setImage(img, autoLevels=False, levels=(0, 255))
        except:
            pass

    def __init_values(self):
        self.img_view = pg.ImageView()
        self.ui.img_widget.layout().addWidget(self.img_view)
        self.img_view.ui.histogram.hide()
        self.img_view.ui.roiBtn.hide()
        self.img_view.ui.menuBtn.hide()

        self.com_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.udp_thread = UdpThread(self)
        self.udp_thread.start()
        self.cap_sig.connect(self.__received_data)
        self.ui.next_btn.clicked.connect(self.__send_next)
        self.ui.revert_btn.clicked.connect(self.__send_revert)

    def __send_next(self):
        self.com_sock.sendto("next".encode("UTF-8"), (Starter.PHONE_IP, Starter.PHONE_PORT))

    def __send_revert(self):
        self.com_sock.sendto("revert".encode("UTF-8"), (Starter.PHONE_IP, Starter.PHONE_PORT))

class UdpThread(QThread):

    def __init__(self, main):
        super().__init__()
        self.main = main
        PORT = 1234
        self.data_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.data_sock.bind(("", Starter.PHONE_PORT))
        self.__running = True

    def run(self):
        while self.__running:
            data, addr = self.data_sock.recvfrom(65536)
            self.main.cap_sig.emit(data.decode("UTF-8"))

    def stop(self):
        self.__running = False


if __name__ == "__main__":
    import sys
    Starter()
