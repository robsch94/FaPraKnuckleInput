package com.example.simon.filmappmaps;


/**
 * Created by Huy on 28/06/2018.
 */

public class ModelDescription {
    public String modelPath;
    public String modelName;
    public String inputNode;
    public String outputNode;
    public long[] inputDimensions;
    public String[] labels;

    public ModelDescription(String modelName, String modelPath, String inputNode, String outputNode, long[] inputDimensions, String[] labels) {
        this.modelName = modelName;
        this.modelPath = modelPath;
        this.inputNode = inputNode;
        this.outputNode = outputNode;
        this.inputDimensions = inputDimensions;
        this.labels = labels;
    }
}

