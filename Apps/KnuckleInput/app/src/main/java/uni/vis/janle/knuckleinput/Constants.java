package uni.vis.janle.knuckleinput;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    public static List<TaskContentDescription> fingerTasks = new ArrayList<>();
    public static List<TaskContentDescription> knuckleTasks = new ArrayList<>();

    public static List<TaskContentDescription> getFingerTasks() {
        if (fingerTasks.isEmpty()) {
            initFingerTasks();
        }
        return fingerTasks;
    }

    public static List<TaskContentDescription> getKnuckleTasks() {
        if (knuckleTasks.isEmpty()) {
            initKnuckleTasks();
        }
        return knuckleTasks;
    }

    private static void initFingerTasks() {
        fingerTasks.add(new TaskContentDescription(17, R.drawable.tap, "Tap", "Finger"));
        fingerTasks.add(new TaskContentDescription(18, R.drawable.twotap, "Two finger tap", "Finger"));
        fingerTasks.add(new TaskContentDescription(19, R.drawable.swipeleft, "Swipe left", "Finger"));
        fingerTasks.add(new TaskContentDescription(20, R.drawable.swiperight, "Swipe right", "Finger"));
        fingerTasks.add(new TaskContentDescription(21, R.drawable.swipeup, "Swipe up", "Finger"));
        fingerTasks.add(new TaskContentDescription(22, R.drawable.swipedown, "Swipe down", "Finger"));
        fingerTasks.add(new TaskContentDescription(23, R.drawable.twoswipeup, "Swipe up with two fingers", "Finger"));
        fingerTasks.add(new TaskContentDescription(24, R.drawable.twoswipedown, "Swipe down with two fingers", "Finger"));
        fingerTasks.add(new TaskContentDescription(25, R.drawable.circle, "Circle", "Finger"));
        fingerTasks.add(new TaskContentDescription(26, R.drawable.arrowheadleft, "Arrowhead left", "Finger"));
        fingerTasks.add(new TaskContentDescription(27, R.drawable.arrowheadright, "Arrowhead right", "Finger"));
        fingerTasks.add(new TaskContentDescription(28, R.drawable.checkmark, "Checkmark", "Finger"));
        fingerTasks.add(new TaskContentDescription(29, R.drawable.flashlight, "Γ", "Finger"));
        fingerTasks.add(new TaskContentDescription(30, R.drawable.l, "L", "Finger"));
        fingerTasks.add(new TaskContentDescription(31, R.drawable.lmirrored, "Mirrored L", "Finger"));
        fingerTasks.add(new TaskContentDescription(32, R.drawable.screenshot, "S", "Finger"));
        fingerTasks.add(new TaskContentDescription(33, R.drawable.rotate, "Press and rotate finger", "Finger"));
    }

    private static void initKnuckleTasks() {
        knuckleTasks.add(new TaskContentDescription(0, R.drawable.tap, "Tap", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(1, R.drawable.twotap, "Two knuckle tap", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(2, R.drawable.swipeleft, "Swipe left", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(3, R.drawable.swiperight, "Swipe right", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(4, R.drawable.swipeup, "Swipe up", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(5, R.drawable.swipedown, "Swipe down", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(6, R.drawable.twoswipeup, "Swipe up with two knuckles", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(7, R.drawable.twoswipedown, "Swipe down with two knuckles", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(8, R.drawable.circle, "Circle", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(9, R.drawable.arrowheadleft, "Arrowhead left", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(10, R.drawable.arrowheadright, "Arrowhead right", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(11, R.drawable.checkmark, "Checkmark", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(12, R.drawable.flashlight, "Γ", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(13, R.drawable.l, "L", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(14, R.drawable.lmirrored, "Mirrored L", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(15, R.drawable.screenshot, "S", "Knuckle"));
        knuckleTasks.add(new TaskContentDescription(16, R.drawable.rotate, "Press and rotate knuckle", "Knuckle"));
    }
}
