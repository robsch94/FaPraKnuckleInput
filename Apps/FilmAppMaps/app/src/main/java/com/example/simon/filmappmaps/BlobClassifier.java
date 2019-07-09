package com.example.simon.filmappmaps;

import android.content.Context;
import android.util.Log;

import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobCoordinates;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.CvType;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.Tensor;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import com.example.simon.filmappmaps.DemoSettings;
import com.example.simon.filmappmaps.ModelDescription;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.threshold;

/**
 * Created by Huy on 05/09/2017.
 */

public class BlobClassifier {
    private Context context;
    private ModelDescription modelDescription;
    private static final ModelDescription cnnModel = DemoSettings.models[0];
    private static final ModelDescription lstmModel = DemoSettings.models[1];
    private static TensorFlowInferenceInterface cnnInferenceInterface;
    private static TensorFlowInferenceInterface lstmInferenceInterface;

    BlobClassifier(Context context) {
        // Loading model from assets folder.
        this.context = context;
        cnnInferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), cnnModel.modelPath);
        lstmInferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), lstmModel.modelPath);
    }

    public ClassificationResult getLSTMIndex(float[] outputs) {
        ClassificationResult cr = new ClassificationResult();
        // Convert concated one-hots
        float maxConf = Float.MIN_VALUE;
        int idxGest = -1;
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] > maxConf) {
                maxConf = outputs[i];
                idxGest = i;
            }
        }
        cr.index = idxGest;
        cr.label = lstmModel.labels[idxGest];
        return cr;
    }

    public ClassificationResult getCNNIndex(float[] outputs) {
        ClassificationResult cr = new ClassificationResult();
        cr.index = (outputs[0] > outputs[1]) ? 0 : 1;
        cr.label = cnnModel.labels[cr.index];
        return cr;
    }

    public ClassificationResult classify(float[] pixels, boolean cnn) {
        TensorFlowInferenceInterface inferenceInterface;
        ModelDescription modelDescription;
        if (cnn) {
            inferenceInterface = cnnInferenceInterface;
            modelDescription = cnnModel;
        }
        else {
            inferenceInterface = lstmInferenceInterface;
            modelDescription = lstmModel;
        }

        // Node Names
        String inputName = modelDescription.inputNode;
        String outputName = modelDescription.outputNode;

        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[modelDescription.labels.length];

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, modelDescription.inputDimensions);
        inferenceInterface.run(outputNodes, true);
        inferenceInterface.fetch(outputName, outputs);

        //return outputs;
        if (cnn) {return getCNNIndex(outputs);}
        else {return getLSTMIndex(outputs);}
    }

    public float[] imagesToPixels(List<int[][]> images) {
        int w = images.get(0)[0].length;
        int h = images.get(0).length;
        float[] pixels = new float[images.size() * w * h];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = images.get(i/(w*h))[(i/w)%h][i%w];
        }
        return pixels;
    }

    public float[] getBlobContentIn27x15(int[][] matrix, BlobBoundingBox bbb) {
        // first extract the blob
        int y1 = Math.max(bbb.y1 - 1, 0);
        int y2 = Math.min(bbb.y2 + 1, 29);
        int x1 = Math.max(bbb.x1 - 1, 0);
        int x2 = Math.min(bbb.x2 + 1, 17);

        int[][] blob = new int[y2-y1][x2-x1];
        for (int y = 0; y < blob.length; y++) {
            for (int x = 0; x < blob[0].length; x++) {
                blob[y][x] = matrix[y1+y][x1+x];
            }
        }

        // put it into new 27x15 image
        float[][] image = new float[27][15];
        for(int y = 0; y < blob.length; y++) {
            for(int x = 0; x < blob[0].length; x++) {
                image[y][x] = blob[y][x];
            }
        }

        float[] result = new float[27*15];
        for(int y = 0; y < 27; y++) {
            for(int x = 0; x < 15; x++) {
                result[x+15*y] = image[y][x];
            }
        }
        return result;
    }

    public int[][] preprocess(CapacitiveImageTS capImg) {
        int[][] matrix = capImg.getMatrix();
        Mat image = int27x15ToPaddedMat(matrix);
        return matToInt2D(image);
    }

    public List<BlobBoundingBox> getBlobBoundaries(int[][] matrix) {
        Mat image = int29x17ToMat(matrix);
        Mat inv_image = new Mat();
        Core.bitwise_not(image, inv_image);
        threshold(inv_image, image, 200, 255, THRESH_BINARY);    //205

        ArrayList<BlobBoundingBox> blobs = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() == 0) {
            return blobs;
        }

        // get max contour
        MatOfPoint max_contour = new MatOfPoint(new Point(5, 5));
        for (int i = 0; i < contours.size(); i++) {
            if (contourArea(contours.get(i)) > 8 && contourArea(contours.get(i)) < 255) {    // > 5
                if (contourArea(contours.get(i)) > contourArea(max_contour)) {
                    max_contour = contours.get(i);
                }
            }
        }

        if (contourArea(max_contour)==contourArea(new MatOfPoint(new Point(5, 5)))) {
            return blobs;
        }

        // get xmin, xmax, ymin, ymax
        int x_min = 2147483647;
        int x_max = -2147483648;
        int y_min = 2147483647;
        int y_max = -2147483648;
        for (Point p : max_contour.toList()) {
            if (p.x < x_min) {
                x_min = (int) p.x;
            }

            if (p.y < y_min) {
                y_min = (int) p.y;
            }

            if (p.x > x_max) {
                x_max = (int) p.x;
            }

            if (p.y > y_max) {
                y_max = (int) p.y;
            }
        }
        BlobBoundingBox bbb = new BlobBoundingBox(x_min, y_min, x_max, y_max);
        blobs.add(bbb);
        return blobs;
    }

    private Mat int27x15ToPaddedMat(int[][] matrix) {
        Mat image = new Mat(29, 17, CvType.CV_8UC1);
        for (int x = 0; x < 29; x++) {
            for (int y = 0; y < 17; y++) {
                image.put(x, y, 1);
            }
        }
        // fill in matrix
        for (int x = 0; x < 27; x++) {
            for (int y = 0; y < 15; y++) {
                image.put(1+x, 1+y, (double) matrix[x][y]);
            }
        }
        return image;
    }

    private Mat int29x17ToMat(int[][] matrix) {
        Mat image = new Mat(29, 17, CvType.CV_8UC1);
        // fill in matrix
        for (int x = 0; x < 29; x++) {
            for (int y = 0; y < 17; y++) {
                image.put(x, y, (double) matrix[x][y]);
            }
        }
        return image;
    }

    private int[][] matToInt2D(Mat mat) {
        int[][] matrix = new int[mat.rows()][mat.cols()];
        for (int x = 0; x < mat.rows(); x++) {
            for (int y = 0; y < mat.cols(); y++) {
                matrix[x][y] = (int) mat.get(x, y)[0];
            }
        }
        return matrix;
    }
}

