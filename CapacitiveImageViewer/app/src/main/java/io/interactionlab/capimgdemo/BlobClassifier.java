package io.interactionlab.capimgdemo;

import android.content.Context;

import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobBoundingBox;
import org.hcilab.libftsp.capacitivematrix.blobdetection.BlobCoordinates;
import org.hcilab.libftsp.capacitivematrix.capmatrix.CapacitiveImageTS;
import org.opencv.core.MatOfInt;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import io.interactionlab.capimgdemo.demo.ModelDescription;

import org.opencv.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    }


    public ClassificationResult classify(float[] pixels) {
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
        MatOfInt image = new MatOfInt();
        ArrayList<BlobBoundingBox> blobs = new ArrayList();

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
}
