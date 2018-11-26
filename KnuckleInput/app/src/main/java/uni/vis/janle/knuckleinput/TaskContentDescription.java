package uni.vis.janle.knuckleinput;

public class TaskContentDescription {
    private String topImage, botImage, gestureText, inputMethodText;


    public TaskContentDescription(String topImage, String botImage, String gestureText, String inputMethodText) {
        this.topImage = topImage;
        this.botImage = botImage;
        this.gestureText = gestureText;
        this.inputMethodText = inputMethodText;
    }

    public String getBotImage() {
        return botImage;
    }

    public void setBotImage(String botImage) {
        this.botImage = botImage;
    }

    public String getTopImage() {
        return topImage;
    }

    public void setTopImage(String topImage) {
        this.topImage = topImage;
    }

    public String getGestureText() {
        return gestureText;
    }

    public void setGestureText(String gestureText) {
        this.gestureText = gestureText;
    }

    public String getInputMethodText() {
        return inputMethodText;
    }

    public void setInputMethodText(String inputMethodText) {
        this.inputMethodText = inputMethodText;
    }
}
