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
            task_id = data.split(";")[1]
            cur_index = data.split(";")[2]
            self.ui.input_lbl.setText(self.match_tasks(task_id))
            self.ui.progress_lbl.setText("%s/420" % cur_index)

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

        self.ui.centralwidget.keyPressEvent = self.keyPressEvent

        self.com_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.udp_thread = UdpThread(self)
        self.udp_thread.start()
        self.cap_sig.connect(self.__received_data)
        self.ui.next_btn.clicked.connect(self.__send_next)
        self.ui.revert_btn.clicked.connect(self.__send_revert)

    def keyPressEvent(self, event):
        if not event.isAutoRepeat():
            if event.key() == QtCore.Qt.Key_R:
                self.__send_next()
            elif event.key() == QtCore.Qt.Key_N:
                self.__send_revert()


    def __send_next(self):
        self.com_sock.sendto("next".encode("UTF-8"), (Starter.PHONE_IP, Starter.PHONE_PORT))

    def __send_revert(self):
        self.com_sock.sendto("revert".encode("UTF-8"), (Starter.PHONE_IP, Starter.PHONE_PORT))

    def match_tasks(self, task_id):
        return { "0": "Finger Tap",
                "1": "Finger TwoTap",
                "2": "Finger SwipeLeft",
                "3": "Finger SwipeRight",
                "4": "Finger SwipeUp",
                "5": "Finger SwipeDown",
                "6": "Finger TwoSwipeUp",
                "7": "Finger TwoSwipeDown",
                "8": "Finger Circle",
                "9": "Finger ArrowheadLeft",
                "10": "Finger ArrowheadRight",
                "11": "Finger Checkmark",
                "12": "Finger Flashlight",
                "13": "Finger L",
                "14": "Finger LMirrored",
                "15": "Finger Screenshot",
                "16": "Finger Rotate",
                "17": "Knuckle Tap",
                "18": "Knuckle TwoTap",
                "19": "Knuckle SwipeLeft",
                "20": "Knuckle SwipeRight",
                "21": "Knuckle SwipeUp",
                "22": "Knuckle SwipeDown",
                "23": "Knuckle TwoSwipeUp",
                "24": "Knuckle TwoSwipeDown",
                "25": "Knuckle Circle",
                "26": "Knuckle ArrowheadLeft",
                "27": "Knuckle ArrowheadRight",
                "28": "Knuckle Checkmark",
                "29": "Knuckle Flashlight",
                "30": "Knuckle L",
                "31": "Knuckle LMirrored",
                "32": "Knuckle Screenshot",
                "33": "Knuckle Rotate"
        }[task_id]

        
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
