from capimageview import CapImageView
from PyQt5 import QtCore, QtGui, QtWidgets
from MainWindow import Ui_mainWindow
from PyQt5.QtCore import QThread, QObject, pyqtSignal
import socket
import pyqtgraph as pg
import numpy as np


class Starter(QObject):

    PHONE_IP = "192.168.1.102"
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
            data_splt = data.split(";")
            """
            result += String.valueOf(userID);
                result += ";" + String.valueOf(System.currentTimeMillis());
                result += ";" + String.valueOf(taskContDescs.size());
                result += ";" + String.valueOf(taskContDescsSize);
                result += ";" + String.valueOf(taskID);
                result += ";" + String.valueOf(versionIDs[taskID]);
                result += ";" + String.valueOf(repititionID);
                result += ";" + String.valueOf(actualData);
                result += ";" + String.valueOf(isPause);
                result += ";" + capImg.toString();
            """
            user_id = data_splt[0]
            time = data_splt[1]
            cur_index = data_splt[2]
            task_amount = data_splt[3]
            task_id = data_splt[4]
            version_id = data_splt[5]
            revert_id = data_splt[6]
            actual_data =data_splt[7]
            is_pause = data_splt[8]
            #self.ui.input_lbl.setText(task_id)
            self.ui.id_lbl.setText(user_id)
            print(is_pause)
            self.ui.input_lbl.setText("Pause" if(is_pause=="true") else self.match_tasks(task_id))
            self.ui.progress_lbl.setText(cur_index + "/"+ task_amount)
            self.ui.input_rev.setText(revert_id)
            self.ui.input_ver.setText(version_id)
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
        return { "17": "Finger Tap",
                "18": "Finger TwoTap",
                "19": "Finger SwipeLeft",
                "20": "Finger SwipeRight",
                "21": "Finger SwipeUp",
                "22": "Finger SwipeDown",
                "23": "Finger TwoSwipeUp",
                "24": "Finger TwoSwipeDown",
                "25": "Finger Circle",
                "26": "Finger ArrowheadLeft",
                "27": "Finger ArrowheadRight",
                "28": "Finger Checkmark",
                "29": "Finger Flashlight",
                "30": "Finger L",
                "31": "Finger LMirrored",
                "32": "Finger Screenshot",
                "33": "Finger Rotate",
                "0": "Knuckle Tap",
                "1": "Knuckle TwoTap",
                "2": "Knuckle SwipeLeft",
                "3": "Knuckle SwipeRight",
                "4": "Knuckle SwipeUp",
                "5": "Knuckle SwipeDown",
                "6": "Knuckle TwoSwipeUp",
                "7": "Knuckle TwoSwipeDown",
                "8": "Knuckle Circle",
                "9": "Knuckle ArrowheadLeft",
                "10": "Knuckle ArrowheadRight",
                "11": "Knuckle Checkmark",
                "12": "Knuckle Flashlight",
                "13": "Knuckle L",
                "14": "Knuckle LMirrored",
                "15": "Knuckle Screenshot",
                "16": "Knuckle Rotate"
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
