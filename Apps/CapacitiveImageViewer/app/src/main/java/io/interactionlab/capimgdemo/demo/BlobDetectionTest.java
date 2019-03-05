package io.interactionlab.capimgdemo.demo;

import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;

import java.util.ArrayList;
import java.util.List;

public class BlobDetectionTest {

    public static final int[][] t1_pre = new int[][]{{2,0,1,2,3,1,4,11,23,13,7,7,1,5,5},
            {0,1,0,0,0,0,0,2,2,1,1,0,0,1,2},
            {0,0,0,0,2,1,1,6,15,10,5,2,4,1,1},
            {1,1,1,1,2,2,1,11,23,15,7,4,4,2,4},
            {2,0,0,0,1,0,2,5,11,4,2,0,0,0,2},
            {0,0,0,0,0,0,0,4,5,4,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,2,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,4,2,2,0,0,0,0,0},
            {0,1,0,1,0,1,2,4,7,1,1,0,1,0,1},
            {0,0,0,1,0,0,0,7,11,1,0,0,0,0,0},
            {4,1,1,0,0,2,7,29,36,16,9,4,4,4,4},
            {0,0,0,0,0,2,11,110,149,29,4,1,0,0,1},
            {2,0,1,2,2,4,23,189,243,46,7,1,1,0,0},
            {0,1,0,0,0,5,23,116,144,26,9,0,0,0,1},
            {0,0,0,0,0,4,10,33,31,10,2,1,0,0,1},
            {1,1,2,2,2,7,10,16,21,13,7,4,2,2,4},
            {0,1,0,0,1,2,1,9,15,9,5,4,5,1,0},
            {1,0,0,1,2,4,2,10,19,16,13,11,5,7,0},
            {0,0,0,0,0,1,1,13,36,50,44,24,11,3,2},
            {0,0,0,0,0,0,0,15,145,194,124,32,7,1,0},
            {0,0,0,0,0,0,0,11,175,238,119,21,7,0,0},
            {0,0,0,0,0,1,0,2,46,74,31,10,9,10,13},
            {0,0,0,0,1,0,1,2,15,16,10,11,18,24,23},
            {0,0,0,0,0,1,0,0,0,0,7,15,27,33,29},
            {0,0,0,0,0,0,0,0,0,0,1,10,29,31,15},
            {0,0,0,0,0,0,0,9,15,10,9,9,15,13,7},
            {1,0,0,0,0,0,0,4,5,7,1,2,5,1,1}};
    public static final int[][] t2_pre = new int[][]{{}};
    public static final int[][] t3_pre = new int[][]{{}};
    public static final int[][] t4_pre = new int[][]{{}};
    public static final int[][] t5_pre = new int[][]{{}};

    public static final List<BlobBoundingBox> t1_post = new ArrayList<BlobBoundingBox>() {{new BlobBoundingBox(6,15,11,21);}};
    public static final List<BlobBoundingBox> t2_post = new ArrayList<>();
    public static final List<BlobBoundingBox> t3_post = new ArrayList<>();
    public static final List<BlobBoundingBox> t4_post = new ArrayList<>();
    public static final List<BlobBoundingBox> t5_post = new ArrayList<>();

}
