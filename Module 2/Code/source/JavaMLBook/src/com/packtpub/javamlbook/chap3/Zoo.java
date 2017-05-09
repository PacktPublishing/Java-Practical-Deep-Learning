package com.packtpub.javamlbook.chap3;

import java.util.Random;

import javax.swing.JFrame;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
//import nz.ac.waikato.cs.weka.Utils;

public class Zoo {

	public static void main(String[] args) throws Exception {

		/*
		 * Load the data
		 */
		DataSource source = new DataSource(args[0]);
		Instances data = source.getDataSet();
		System.out.println(data.numInstances() + " instances loaded.");
		// System.out.println(data.toString());

		// remove animal attribute
		String[] opts = new String[] { "-R", "1" };
		Remove remove = new Remove();
		remove.setOptions(opts);
		remove.setInputFormat(data);
		data = Filter.useFilter(data, remove);

		/*
		 * Feature selection
		 */
		AttributeSelection attSelect = new AttributeSelection();
		InfoGainAttributeEval eval = new InfoGainAttributeEval();
		Ranker search = new Ranker();
		attSelect.setEvaluator(eval);
		attSelect.setSearch(search);
		attSelect.SelectAttributes(data);
		int[] indices = attSelect.selectedAttributes();
//		System.out.println(Utils.arrayToString(indices));

		/*
		 * Decision trees
		 */
		String[] options = new String[1];
		options[0] = "-U";
		J48 tree = new J48();
		tree.setOptions(options);
		tree.buildClassifier(data);
		System.out.println(tree);

		/*
		 * Classify new instance.
		 */
		double[] vals = new double[data.numAttributes()];
		vals[0] = 1.0; // hair {false, true}
		vals[1] = 0.0; // feathers {false, true}
		vals[2] = 0.0; // eggs {false, true}
		vals[3] = 1.0; // milk {false, true}
		vals[4] = 0.0; // airborne {false, true}
		vals[5] = 0.0; // aquatic {false, true}
		vals[6] = 0.0; // predator {false, true}
		vals[7] = 1.0; // toothed {false, true}
		vals[8] = 1.0; // backbone {false, true}
		vals[9] = 1.0; // breathes {false, true}
		vals[10] = 1.0; // venomous {false, true}
		vals[11] = 0.0; // fins {false, true}
		vals[12] = 4.0; // legs INTEGER [0,9]
		vals[13] = 1.0; // tail {false, true}
		vals[14] = 1.0; // domestic {false, true}
		vals[15] = 0.0; // catsize {false, true}
		// Instance myUnicorn = new Instance(1.0, vals);
		//
		// double result = tree.classifyInstance(myUnicorn);
		// System.out.println(data.classAttribute().value((int) result));

		/*
		 * Visualize decision tree
		 */
		TreeVisualizer tv = new TreeVisualizer(null, tree.graph(),
				new PlaceNode2());
		JFrame frame = new javax.swing.JFrame("Tree Visualizer");
		frame.setSize(800, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(tv);
		frame.setVisible(true);
		tv.fitToScreen();

		// evaluation

		Classifier cl = new J48();
		Evaluation eval_roc = new Evaluation(data);
		eval_roc.crossValidateModel(cl, data, 10, new Random(1),
				new Object[] {});
		System.out.println(eval_roc.toSummaryString());
		/*
		 * Confusion matrix
		 */
		double[][] confusionMatrix = eval_roc.confusionMatrix();
		System.out.println(eval_roc.toMatrixString());
		// for (int i = 0; i < confusionMatrix.length; i++) {
		// System.out.print(data.classAttribute().value(i) + "\t\t");
		// for (int j = 0; j < confusionMatrix.length; j++) {
		// System.out.print(confusionMatrix[i][j] + "\t");
		// }
		// System.out.println();
		// }

		/*
		 * ROC
		 */

		ThresholdCurve tc = new ThresholdCurve();
		int classIndex = 0;
		Instances result = tc.getCurve(eval_roc.predictions(), classIndex);
		// plot curve
		ThresholdVisualizePanel vmc = new ThresholdVisualizePanel();
		vmc.setROCString("(Area under ROC = " + tc.getROCArea(result) + ")");
		vmc.setName(result.relationName());
		PlotData2D tempd = new PlotData2D(result);
		tempd.setPlotName(result.relationName());
		tempd.addInstanceNumberAttribute();
		// specify which points are connected
		boolean[] cp = new boolean[result.numInstances()];
		for (int n = 1; n < cp.length; n++)
			cp[n] = true;
		tempd.setConnectPoints(cp);

		// add plot
		vmc.addPlot(tempd);
		// display curve
		JFrame frameRoc = new javax.swing.JFrame("ROC Curve");
		frameRoc.setSize(800, 500);
		frameRoc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameRoc.getContentPane().add(vmc);
		frameRoc.setVisible(true);

		/*
		 * Other learning algorithms
		 */

	}
}
