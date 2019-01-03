package io.interactionlab.capimgdemo;

import android.util.Log;

import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.opencv.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

//import static org.opencv.imgproc.Imgproc.CV_RETR_TREE;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.accumulateSquare;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.moments;
import static org.opencv.imgproc.Imgproc.threshold;


public class ContourDetection {

    public static int[][] getBlobContentIn27x15(int[][] matrix, BlobBoundingBox bbb) {
        Log.i("deftest",matrix.toString());
        Mat image = new Mat();
        image.create(matrix.length,matrix[0].length, CV_8UC1); // 8-bit single channel image
        for (int i=0; i<matrix.length; i++) {
            for(int j=0; j<matrix[0].length; j++) {
                image.put(i,j, matrix[i][j]);
            }
        }
        Log.i("deftest", image.toString());
        //Mat inv_image = new Mat();
        Core.bitwise_not(image, image);
        threshold(image, image, 200, 255, THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();
        // CV_RETR_TREE=3, CV_CHAIN_APPROX_SIMPLE=2
        findContours(image, contours, new Mat(), 3, 2, new Point());

        List<MatOfPoint> contours_filtered = new ArrayList<>();
        MatOfPoint max_contour = new MatOfPoint();
        for (int i = 0; i < contours.size(); i++) {
            if (contourArea(contours.get(i)) > 5 && contourArea(contours.get(i)) > 255) {
                contours_filtered.add(contours.get(i));
                if (contourArea(contours.get(i)) > contourArea(max_contour)) {
                    max_contour = contours.get(i);
                }
            }
        }

        int xmin = 10000;
        int xmax = -10000;
        int ymin = 10000;
        int ymax = -10000;
        for (Point point : max_contour.toArray()) {
            if (point.x < xmin) {xmin = (int) point.x;}
            if (point.x > xmax) {xmax = (int) point.x;}
            if (point.y < ymin) {ymin = (int) point.y;}
            if (point.x > ymax) {ymax = (int) point.y;}
        }

        if (!contours_filtered.isEmpty()) {
            double a = Collections.max(Arrays.asList(1.0,0.0));
            double[][] blob = new double[27][15];
            Mat blob_mat = image.submat(Collections.max(Arrays.asList(ymin - 1, 0)), Collections.min(Arrays.asList(ymax + 1, image.width())), Collections.max(Arrays.asList(xmin - 1, 0)), Collections.min(Arrays.asList(xmax + 1, image.height())));
            for (int row = 0; row < 27; row++) {
                blob[row] = blob_mat.get(0, 0);
            }

            int[][] croped_im = new int[27][15];
            for (int i = 0; i < blob.length; i++) {
                for (int j = 0; j < blob[0].length; j++) {
                    croped_im[i][j] = (int) blob[i][j];
                }
            }
            return croped_im;
        }
        return new int[27][15];
        /*
        if len(contours) > 0:
            contours.sort(key=lambda a: cv2.contourArea(a))
            max_contour = contours[-1]
            xmax, ymax = np.max(max_contour.reshape(len(max_contour),2), axis=0)
            xmin, ymin = np.min(max_contour.reshape(len(max_contour),2), axis=0)
            croped_im = np.zeros((27,15))
            blob = image[max(ymin - 1, 0):min(ymax + 1, image.shape[0]),max(xmin - 1, 0):min(xmax + 1, image.shape[1])]
            croped_im[0:blob.shape[0],0:blob.shape[1]] = blob
            return (1, [croped_im])
        else:
            return (0, np.zeros((27,15)))
         */
    }
}
