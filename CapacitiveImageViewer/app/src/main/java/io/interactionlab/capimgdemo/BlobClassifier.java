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
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import io.interactionlab.capimgdemo.demo.ModelDescription;

import java.util.ArrayList;
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

    public BlobClassifier(Context context) {
        // Loading model from assets folder.
        this.context = context;
    }

    public void setModel(ModelDescription modelDescription) {
        this.modelDescription = modelDescription;
        inferenceInterface = new TensorFlowInferenceInterface(context.getAssets(), modelDescription.modelPath);
        Log.i("Test", "Initialisation of inferenceInterface: "+String.valueOf(inferenceInterface));
    }


    public ClassificationResult classify(float[] pixels) {
        // Node Names
        String inputName = modelDescription.inputNode;
        String outputName = modelDescription.outputNode;


        // Define output nodes
        String[] outputNodes = new String[]{outputName};
        float[] outputs = new float[modelDescription.labels.length];

        Log.i("Test", "inferenceInterface: "+String.valueOf(inferenceInterface));
        // Feed image into the model and fetch the results.
        inferenceInterface.feed(inputName, pixels, modelDescription.inputDimensions);
        inferenceInterface.run(outputNodes, true);
        inferenceInterface.fetch(outputName, outputs);


        // Convert one-hot encoded result to an int (= detected class)
        float maxConf = Float.MIN_VALUE;
        int idx = -1;
        for (int i = 0; i < outputs.length; i++) {
            if (outputs[i] > maxConf) {
                maxConf = outputs[i];
                idx = i;
            }
        }

        ClassificationResult cr = new ClassificationResult();
        cr.index = idx;
        cr.label = modelDescription.labels[idx];
        cr.confidence = maxConf;
        cr.color = modelDescription.labelColor[idx];

        return cr;
    }

    public int[][] getBlobContentIn27x15(int[][] matrix, BlobBoundingBox bbb) {
        int[][] blob = new int[27][15];
        int delta_x = bbb.x2 - bbb.x1;
        int delta_y = bbb.y2 - bbb.y1;

        for(int y = 0; y < delta_y; y++) {
            for(int x = 0; x < delta_x; x++) {
                blob[y][x] = matrix[bbb.y1 + y][bbb.x1 + x];
            }
        }

        return blob;
    }

    public List<BlobBoundingBox> getBlobBoundaries(CapacitiveImageTS capImg) {
        int[][] matrix = capImg.getMatrix();
        ArrayList<BlobBoundingBox> blobs = new ArrayList<>();

        // find contours of image
        Mat image = int27x15ToMat(matrix);
        Mat inv_image = new Mat();
        Core.bitwise_not(image, inv_image);
        threshold(inv_image, image, 200, 255, THRESH_BINARY);
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


        /*
        for(int y = 0; y < matrix.length; ++y) {
            for(int x = 0; x < matrix[0].length; ++x) {
                List<BlobCoordinates> found = new ArrayList();
                blobDetection(matrix, x, y, found);
                if (found.size() > 0) {
                    int x_min = 2147483647;
                    int x_max = -2147483648;
                    int y_min = 2147483647;
                    int y_max = -2147483648;
                    Iterator var10 = found.iterator();

                    while(var10.hasNext()) {
                        BlobCoordinates b = (BlobCoordinates)var10.next();
                        if (b.x1 < x_min) {
                            x_min = b.x1;
                        }

                        if (b.y1 < y_min) {
                            y_min = b.y1;
                        }

                        if (b.x1 > x_max) {
                            x_max = b.x1;
                        }

                        if (b.y1 > y_max) {
                            y_max = b.y1;
                        }
                    }

                    BlobBoundingBox bbb = new BlobBoundingBox(x_min - 1, y_min - 1, x_max + 1, y_max + 1);
                    if (!blobs.contains(bbb) && (x_max - x_min) * (y_max - y_min) > 1) {
                        blobs.add(bbb);
                    }
                }
            }
        }

        return blobs;
        */
        /*
        #Svens new Blob detection
        def detect_blobs(image, task):
            #image = e.Image
            large = np.ones((29,17), dtype=np.uint8)
            large[1:28,1:16] = np.copy(image)
            temp, thresh = cv2.threshold(cv2.bitwise_not(large), 205, 255, cv2.THRESH_BINARY)
            im2, contours, hierarchy = cv2.findContours(thresh, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
            contours = [a for a in contours if cv2.contourArea(a) > 8 and cv2.contourArea(a) < 255]
            lstBlob  = []
            lstMin = []
            lstMax = []
            count = 0
            contours.sort(key=lambda a: cv2.contourArea(a))
            if len(contours) > 0:
                # if two finger or knuckle
                cont_count = 2 if task in [6, 7, 23, 24] and len(contours) > 1 else 1
                for i in range(1, cont_count + 1):
                    max_contour = contours[-1 * i]
                    xmax, ymax = np.max(max_contour.reshape(len(max_contour),2), axis=0)
                    xmin, ymin = np.min(max_contour.reshape(len(max_contour),2), axis=0)
                    #croped_im = np.zeros((27,15))
                    blob = large[max(ymin - 1, 0):min(ymax + 1, large.shape[0]),max(xmin - 1, 0):min(xmax + 1, large.shape[1])]
                    #croped_im[0:blob.shape[0],0:blob.shape[1]] = blob
                    #return (1, [croped_im])
                    lstBlob.append(blob)
                    lstMin.append(xmax-xmin)
                    lstMax.append(ymax-ymin)
                    count = count + 1
                return (count, lstBlob, lstMin, lstMax)
            else:
                return (0, [np.zeros((29, 19))], 0, 0)
       */
        /*
        def pasteToEmpty (blob):
        croped_im = np.zeros((27,15))
        croped_im[0:blob.shape[0],0:blob.shape[1]] = blob
        return croped_im
        */
    }

    private void blobDetection(int[][] matrix, int x, int y, List<BlobCoordinates> found) {
       if (x > 0 && x < matrix[0].length && y > 0 && y < matrix.length && matrix[y][x] > 30 && !found.contains(new BlobCoordinates(x, y))) {
            found.add(new BlobCoordinates(x, y));
            blobDetection(matrix, x + 1, y, found);
            blobDetection(matrix, x - 1, y, found);
            blobDetection(matrix, x, y + 1, found);
            blobDetection(matrix, x, y - 1, found);
        }
    }

    public Mat int27x15ToMat(int[][] matrix) {
        Mat image = new Mat(27, 15, CvType.CV_8UC1);
        for (int x = 0; x < 27; x++) {
            for (int y = 0; y < 15; y++) {
                //Log.i("Test", String.valueOf(matrix[x][y]));
                image.put(x, y, (double) matrix[x][y]);
            }
        }
        return image;
    }
}
