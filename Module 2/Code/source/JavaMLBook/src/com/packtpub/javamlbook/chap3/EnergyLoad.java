package com.packtpub.javamlbook.chap3;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.core.Instances;
//import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class EnergyLoad {

	public static void main(String[] args) throws Exception {

		/*
		 * Load data
		 */
		CSVLoader loader = new CSVLoader();
		loader.setFieldSeparator(",");
		loader.setSource(new File(args[0]));
		Instances data = loader.getDataSet();

		// System.out.println(data);

		/*
		 * Build regression models
		 */
		// set class index to Y1 (heating load)
		data.setClassIndex(data.numAttributes() - 2);
		// remove last attribute Y2
		Remove remove = new Remove();
		remove.setOptions(new String[] { "-R", data.numAttributes() + "" });
		remove.setInputFormat(data);
		data = Filter.useFilter(data, remove);

		// build a regression model
		LinearRegression model = new LinearRegression();
		model.buildClassifier(data);
		System.out.println(model);

		// 10-fold cross-validation
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(model, data, 10, new Random(1), new String[] {});
		System.out.println(eval.toSummaryString());
		double coef[] = model.coefficients();
		System.out.println();

		// build a regression tree model

		M5P md5 = new M5P();
		md5.setOptions(new String[] { "" });
		md5.buildClassifier(data);
		System.out.println(md5);

		// 10-fold cross-validation
		eval.crossValidateModel(md5, data, 10, new Random(1), new String[] {});
		System.out.println(eval.toSummaryString());
		System.out.println();

		// ZeroR modelZero = new ZeroR();
		//
		//
		//
		//
		//
		// REPTree modelTree = new REPTree();
		// modelTree.buildClassifier(data);
		// System.out.println(modelTree);
		// eval = new Evaluation(data);
		// eval.crossValidateModel(modelTree, data, 10, new Random(1), new
		// String[]{});
		// System.out.println(eval.toSummaryString());
		//
		// SMOreg modelSVM = new SMOreg();
		//
		// MultilayerPerceptron modelPerc = new MultilayerPerceptron();
		//
		// GaussianProcesses modelGP = new GaussianProcesses();
		// modelGP.buildClassifier(data);
		// System.out.println(modelGP);
		// eval = new Evaluation(data);
		// eval.crossValidateModel(modelGP, data, 10, new Random(1), new
		// String[]{});
		// System.out.println(eval.toSummaryString());

		// save ARFF
		// ArffSaver saver = new ArffSaver();
		// saver.setInstances(data);
		// saver.setFile(new File(args[1]));
		// saver.setDestination(new File(args[1]));
		// saver.writeBatch();

	}

}
