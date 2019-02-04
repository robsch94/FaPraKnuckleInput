package io.interactionlab.capimgdemo;

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

import io.interactionlab.capimgdemo.demo.BlobDetectionTest;
import io.interactionlab.capimgdemo.demo.ModelDescription;

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
    private static TensorFlowInferenceInterface inferenceInterface;
    private Context context;
    private ModelDescription modelDescription;

    BlobClassifier(Context context) {
        // Loading model from assets folder.
        this.context = context;
    }

    public void setModel(ModelDescription modelDescription) {
        this.modelDescription = modelDescription;
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelDescription.modelPath);
        //Log.i("Test", "Initialisation of inferenceInterface: "+String.valueOf(inferenceInterface));
    }

    public ClassificationResult classify(float[] pixels, boolean lstm) {
        // Node Names
        String inputName = modelDescription.inputNode;
        String outputName = modelDescription.outputNode;

        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[modelDescription.labels.length];

        //Log.i("Test", "inferenceInterface: "+String.valueOf(inferenceInterface));
        //Tensor a = Tensor.create(modelDescription.inputDimensions, FloatBuffer.wrap(pixels));
        //Log.i("Test", "Tensor: "+a.toString());

        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, modelDescription.inputDimensions);
        inferenceInterface.run(outputNodes, true);
        inferenceInterface.fetch(outputName, outputs);

        ClassificationResult cr = new ClassificationResult();
        if (lstm) {
            /*
            // Convert concated one-hots
            float maxConf = Float.MIN_VALUE;
            float maxConfGest = Float.MIN_VALUE;
            float maxConfInput = Float.MIN_VALUE;
            int idxGest = -1;
            int idxInput = -1;
            for (int i = 0; i < outputs.length-2; i++) {
                if (outputs[i] > maxConfGest) {
                    maxConfGest = outputs[i];
                    idxGest = i;
                }
            }
            if (outputs[outputs.length-1] > outputs[outputs.length-2]) {
                idxInput = outputs.length-1;
                maxConfInput = outputs[outputs.length-1];
            } else {
                idxInput = outputs.length-2;
                maxConfInput = outputs[outputs.length-2];
            }

            float norm = 0.0f;
            for (int i = 0; i < outputs.length; i++) {
                norm += outputs[i];
            }
            maxConf = (maxConfGest + maxConfInput) / norm;

            cr.index = idxGest;
            cr.label = modelDescription.labels[idxInput]+": "+modelDescription.labels[idxGest];
            cr.confidence = maxConf;
            cr.color = modelDescription.labelColor[idxGest];
            */
            // Convert concated one-hots
            float maxConf = Float.MIN_VALUE;
            int idxGest = -1;
            for (int i = 0; i < outputs.length; i++) {
                if (outputs[i] > maxConf) {
                    maxConf = outputs[i];
                    idxGest = i;
                }
            }
            float norm = 0.0f;
            for (int i = 0; i < outputs.length; i++) {
                norm += outputs[i];
            }
            maxConf = maxConf / norm;

            cr.index = idxGest;
            cr.label = modelDescription.labels[idxGest];
            cr.confidence = maxConf;
            cr.color = modelDescription.labelColor[idxGest];
        } else {
            // Convert one-hot encoded result to an int (= detected class)
            float maxConf = Float.MIN_VALUE;
            int idx = -1;
            for (int i = 0; i < outputs.length; i++) {
                if (outputs[i] > maxConf) {
                    maxConf = outputs[i];
                    idx = i;
                }
            }

            float norm = 0.0f;
            for (int i = 0; i < outputs.length; i++) {
                norm += outputs[i];
            }
            maxConf = maxConf / norm;

            cr.index = idx;
            cr.label = modelDescription.labels[idx];
            cr.confidence = maxConf;
            cr.color = modelDescription.labelColor[idx];
        }
        return cr;
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
        //Log.i("Test", "Matrix for blob content: "+Arrays.deepToString(matrix));
        int y1 = Math.max(bbb.y1 - 1, 0);
        int y2 = Math.min(bbb.y2 + 1, 29);
        int x1 = Math.max(bbb.x1 - 1, 0);
        int x2 = Math.min(bbb.x2 + 1, 17);

        int[][] blob = new int[y2-y1][x2-x1];
        for (int y = 0; y < blob.length; y++) {
            for (int x = 0; x < blob[0].length; x++) {
                blob[y][x] = matrix[y1+y][x1+x];
                //ArrayIndexOutOfBoundsExepction when finger on the right side of screen
            }
        }

        // put it into new 27x15 image
        float[][] image = new float[27][15];
        for(int y = 0; y < blob.length; y++) {
            for(int x = 0; x < blob[0].length; x++) {
                image[y][x] = blob[y][x];
            }
        }
        //Log.i("Test", "Blob: \n" + Arrays.deepToString(blob));
        //Log.i("Test", "Final image: \n" + Arrays.deepToString(image));

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
        //TODO: Just for testing purposes
        //matrix = BlobDetectionTest.t1_pre;

        // find contours of image
        Mat image = int27x15ToPaddedMat(matrix);
        //Log.i("Test", "Image after int27x15ToMat: \n"+image.dump());

        return matToInt2D(image);
    }

    public List<BlobBoundingBox> getBlobBoundaries(int[][] matrix) {
        //TODO: Just for testing purposes
        //matrix = BlobDetectionTest.t1_pre;
        Mat image = int29x17ToMat(matrix);
        Mat inv_image = new Mat();
        Core.bitwise_not(image, inv_image);
        //Mat image = new Mat();
        //Mat inv_image = int29x17ToMat(matrix);
        threshold(inv_image, image, 205, 255, THRESH_BINARY);
        //Log.i("Test", "Image after threshold: \n"+image.dump());

        ArrayList<BlobBoundingBox> blobs = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if (contours.size() == 0) {
            return blobs;
        }

        // get max contour
        MatOfPoint max_contour = new MatOfPoint(new Point(5, 5));
        for (int i = 0; i < contours.size(); i++) {
            if (contourArea(contours.get(i)) > 5 && contourArea(contours.get(i)) < 255) {
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
        // The BlobBoundingBox is right!!!
        blobs.add(bbb);
        return blobs;
    }

    private Mat int27x15ToPaddedMat(int[][] matrix) {
        Mat image = new Mat(29, 17, CvType.CV_8UC1);
        // np.ones((29,17))
        for (int x = 0; x < 29; x++) {
            for (int y = 0; y < 17; y++) {
                image.put(x, y, 1);
            }
        }
        // fill in matrix
        for (int x = 0; x < 27; x++) {
            for (int y = 0; y < 15; y++) {
                //Log.i("Test", String.valueOf(matrix[x][y]));
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
