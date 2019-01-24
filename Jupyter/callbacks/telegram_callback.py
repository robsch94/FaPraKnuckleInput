import telegram
from keras.callbacks import Callback


class TelegramCallback(Callback):

    def __init__(self, config):
        super(TelegramCallback, self).__init__()
        self.user_id = config['telegram_id']
        self.model_name = config['model_name']
        self.bot = telegram.Bot(config['token'])

    def send_message(self, text):
        try:
            self.bot.send_message(chat_id=self.user_id, text=text)
        except Exception as e:
            print('Message did not send. Error: {}.'.format(e))

    def on_train_begin(self, logs={}):
        text = 'Start training model {} | {}'.format(self.model.name, self.model_name)
        self.send_message(text)

    def on_epoch_end(self, epoch, logs={}):
        text = '{} | Epoch {}.\n'.format(self.model_name, epoch)
        for k, v in logs.items():
            text += '{}: {:.4f}; '.format(k, v)
        self.send_message(text)