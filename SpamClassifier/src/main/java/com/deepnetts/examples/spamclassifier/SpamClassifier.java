package com.deepnetts.examples.spamclassifier;

import deepnetts.data.DataSets;
import deepnetts.data.norm.MaxNormalizer;
import deepnetts.eval.Evaluators;
import javax.visrec.ml.eval.EvaluationMetrics;
import deepnetts.net.FeedForwardNetwork;
import deepnetts.net.layers.activation.ActivationType;
import deepnetts.net.loss.LossType;
import deepnetts.util.DeepNettsException;
import java.io.IOException;
import javax.visrec.ml.ClassificationException;
import javax.visrec.ml.classification.BinaryClassifier;
import javax.visrec.ml.data.DataSet;
import javax.visrec.ri.ml.classification.FeedForwardNetBinaryClassifier;
import deepnetts.data.MLDataItem;

/**
 * Spam  Classification example.
 * This example shows how to create basic machine learning model for spam classification using neural networks.
 * Based on the set of example emails  CSV file given as data set, the model will learn to predict whether some given email is spam or not.
 * The same workflow can be applied for other similar classification problems and different data, with small adjustments.
 *
 * Step by step tutorial is available at.
 * Additional concise explanations of the basic machine learning steps and neural network concepts
 * are given as links in code comments.
 * See {@link <a href="TBD">Spam Classification Article</a>}
 *
 * version 0.1 by Vic
 * @author Zoran Sevarac <zoran.sevarac@deepnetts.com>
 */
public class SpamClassifier {
    
    public static void main(String[] args) throws DeepNettsException, IOException, ClassificationException {
                
        String csvFile = "spam.csv"; // CSV file with spam data. For more about CSV files see http://www.deepnetts.com/blog/terms#csv
        int numInputs = 57;          // 57 input features which are used to determine if email is a spam (capital letters, specific words etc. for details see ... )
        int numOutputs = 1;          // and one output which indicates if email is a spam or not  
        
        // Load data set from CSV file and create instance of data set.  
        // To learn more about data sets used for machine learning see http://www.deepnetts.com/blog/terms#data-set
        DataSet<MLDataItem> exampleEmailsDataSet = DataSets.readCsv(csvFile, numInputs, numOutputs, true);             

        // Split data set into training and test set. 
        // To understand why this needs to be done see http://www.deepnetts.com/blog/terms#training-test-split
        DataSet<MLDataItem>[] trainAndTestSet = exampleEmailsDataSet.split(0.6, 0.4);
        DataSet<MLDataItem> trainingSet = trainAndTestSet[0];
        DataSet<MLDataItem> testSet = trainAndTestSet[1];
        
        // PREPARE DATA: Perform max normalization. 
        // To undertsand why is normalization important and how to do it properly see http://www.deepnetts.com/blog/terms#normalization
        MaxNormalizer norm = new MaxNormalizer(trainingSet); // create and initialize normalizer using training set
        norm.normalize(trainingSet); // normalize training set
        norm.normalize(testSet); // normalize test set
        
        // Create an instance of the Feed Forward Neural Network using builder. 
        // To understand structure and components of the neural network see http://www.deepnetts.com/blog/terms#feed-forward-net
        FeedForwardNetwork neuralNet = FeedForwardNetwork.builder()
                .addInputLayer(numInputs)
                .addFullyConnectedLayer(15)
                .addOutputLayer(numOutputs, ActivationType.SIGMOID)
                .lossFunction(LossType.CROSS_ENTROPY)
                .randomSeed(123)
                .build();

        // CONFIGURE LEARNING ALGORITHM. 
        // For more about training and available settings see http://www.deepnetts.com/blog/terms#network-training
        neuralNet.getTrainer().setMaxError(0.03f)        // training stops when when this error is reached
                              .setMaxEpochs(10000)       // or if it has been running for specified number of epochs
                              .setLearningRate(0.001f);  // size of the step for adjuting internal parameters that learning algorithm takes in each iteration
        
        // TRAIN: Start training. To understand training output see link
        neuralNet.train(trainingSet);
        
        // TEST: test network /  evaluate classifier.
        // To understand classifier evaluation see http://www.deepnetts.com/blog/terms#evaluation
        EvaluationMetrics em = Evaluators.evaluateClassifier(neuralNet, testSet);
        System.out.println(em);
                        
        // HOW TO USE IT: create a binary classifier using trained network, and use it through user friendly Java ML API. 
        // To understand what is a binary classifier see http://www.deepnetts.com/blog/terms#binary-classifier
        BinaryClassifier<float[]> binaryClassifier = new FeedForwardNetBinaryClassifier(neuralNet);        
                
        // get a test email from the test set
        float[] testEmail = testSet.get(0).getInput().getValues(); // get some email features to check if it is a spam or not
        Float result = binaryClassifier.classify(testEmail);   // This is how you use trained l model in your app
        
        System.out.println("Spam probability for the given input: "+result);    
        
    }
    
}