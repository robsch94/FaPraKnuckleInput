package uni.vis.janle.knuckleinput;

public class TaskContentDescription {
    private int image;
    private String gestureText, inputMethodText;

    public TaskContentDescription(int image, String gestureText, String inputMethodText) {
        this.image = image;
        this.gestureText = gestureText;
        this.inputMethodText = inputMethodText;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
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
