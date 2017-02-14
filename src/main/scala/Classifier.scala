import java.io.File

import org.apache.spark.mllib.classification.{LogisticRegressionModel, LogisticRegressionWithLBFGS, NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.tree.{DecisionTree, RandomForest}
import org.apache.spark.mllib.tree.model.{DecisionTreeModel, RandomForestModel}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.{SVMModel, SVMWithSGD}

/**
  * Created by fadhilurrizki on 28/01/17.
  */
object Classifier {
  val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("scala-ssentiment-park")
  val sc = new SparkContext(conf)

  def train(classifier : String, features : List[List[Double]], label : List[Int], filename : String): Unit = {
    var featuree = List[LabeledPoint]()
    for (i <- 0 to features.size-1) {
      featuree :+= LabeledPoint(label(i), Vectors.dense(features(i).toArray))
    }
    val trainingData = sc.parallelize(featuree)

    classifier match {
      case "logistic-regression" => {
        val numClasses = 2
        val model = new LogisticRegressionWithLBFGS().setNumClasses(numClasses).run(trainingData)
        val dir = new File(filename)
        delete(dir)
        model.save(sc, filename)
      }
      case "naive-bayes" => {
        val model = NaiveBayes.train(trainingData, lambda = 1.0, modelType = "multinomial")
        val dir = new File(filename)
        delete(dir)
        model.save(sc, filename)
      }
      case "random-forest" => {
        val numClasses = 2
        val categoricalFeaturesInfo = Map[Int, Int]()
        val numTrees = 3 // Use more in practice.
        val featureSubsetStrategy = "auto" // Let the algorithm choose.
        val impurity = "gini"
        val maxDepth = 4
        val maxBins = 32
        val model = RandomForest.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
          numTrees, featureSubsetStrategy, impurity, maxDepth, maxBins)
        val dir = new File(filename)
        delete(dir)
        model.save(sc, filename)
      }
      case "decision-tree" => {
        val numClasses = 3
        val categoricalFeaturesInfo = Map[Int, Int]()
        val numTrees = 3 // Use more in practice.
        val featureSubsetStrategy = "auto" // Let the algorithm choose.
        val impurity = "gini"
        val maxDepth = 4
        val maxBins = 32
        val model = DecisionTree.trainClassifier(trainingData, numClasses, categoricalFeaturesInfo,
          impurity, maxDepth, maxBins)
        val dir = new File(filename)
        delete(dir)
        model.save(sc, filename)
      }
      case "svm" => {
        val numIterations = 100
        val model = SVMWithSGD.train(trainingData, numIterations)
        val dir = new File(filename)
        delete(dir)
        model.save(sc, filename)
      }
    }


  }

  def predict(classifier : String, features : List[List[Double]], filename : String) : List[Int] = {
    var result = List[Int]()
    classifier match {
      case "logistic-regression" => {
        val model = LogisticRegressionModel.load(sc, filename)
        for(feature <- features) {
          result :+= model.predict(Vectors.dense(feature.toArray)).toInt
        }
      }
      case "naive-bayes" => {
        val model = NaiveBayesModel.load(sc, filename)
        for(feature <- features) {
          result :+= model.predict(Vectors.dense(feature.toArray)).toInt
        }
      }
      case "random-forest" => {
        val model = RandomForestModel.load(sc, filename)
        for(feature <- features) {
          result :+= model.predict(Vectors.dense(feature.toArray)).toInt
        }
      }
      case "decision-tree" => {
        val model = DecisionTreeModel.load(sc, filename)
        for(feature <- features) {
          result :+= model.predict(Vectors.dense(feature.toArray)).toInt
        }
      }
      case "svm" => {
        val model = SVMModel.load(sc, filename)
        for(feature <- features) {
          result :+= model.predict(Vectors.dense(feature.toArray)).toInt
        }
      }
    }

    result
  }

  def delete(file: File) {
    if (file.isDirectory)
      Option(file.listFiles).map(_.toList).getOrElse(Nil).foreach(delete(_))
    file.delete
  }
}
