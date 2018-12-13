from capimageview import CapImageView
from PyQt5 import QtCore, QtGui, QtWidgets
from MainWindow import Ui_mainWindow
from PyQt5.QtCore import QThread, QObject, pyqtSignal
import socket


class Starter(QObject):

    PHONE_IP = "127.0.0.1"
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
        self.ui.CapImage.set_image(None)

    def __received_data(self, data):
        print(data)

    def __init_values(self):
        self.com_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.udp_thread = UdpThread(self)
        self.udp_thread.start()
        self.cap_sig.connect(self.__received_data)

    def __send_next(self):
        self.com_sock.sendto("next".encode("UTF-8"), (Starter.PHONE_IP, Starter.PHONE_PORT))

    def __send_revert(self):
        pass

class UdpThread(QThread):

    def __init__(self, main):
        super().__init__()
        self.main = main
        PORT = 1234
        self.data_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.data_sock.bind(("127.0.0.1", Starter.PHONE_PORT))
        self.__running = True

    def run(self):
        while self.__running:
            data, addr = self.data_sock.recvfrom(16384)
            self.main.cap_sig.emit(data.decode("UTF-8"))

    def stop(self):
        self.__running = False


if __name__ == "__main__":
    import sys
    Starter()
