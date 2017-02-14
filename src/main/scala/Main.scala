import java.io.{File, FileInputStream, PrintWriter}

import org.apache.poi.ss.usermodel.WorkbookFactory

import scala.io.Source

/**
  * Created by fadhilurrizki on 20/01/17.
  */
object Main {

  def testAll(args : Array[String]) : Unit = {
    var dataset = (List[String](), List[Int]())
    val datatype = args(0)
    val path = args(1)
    println("LOADING DATA...")
    datatype match {
      case "txt" => dataset = Dataset.loadTxt(path)
      case "xls" => dataset = Dataset.loadExcel(path)
      // catch the default with a variable so you can print it
      case whoa  => println("Unexpected case: " + whoa.toString)
    }

    var stopFlag = Array(true,false)
    var stemFlag = Array(true,false)
    var percenttraining = Array(0.5, 0.6, 0.7, 0.8)
    var featurecase = Array(/*"ngram", "tfidf",*/ "cfidf")
    var n = Array(1,2,3,4,5,6)
    var min_df = Array(0, 0.001, 0.005, 0.01, 0.05, 0.1)
    var max_df = Array(1, 0.97, 0.93, 0.9, 0.8, 0.7)
    var freqFlag = Array(true, false)
    val classifiers = Array("logistic-regression", "naive-bayes", "random-forest", "decision-tree","svm")
    var max_acc = 0.0

    //initiate excel output file
    val file = new FileInputStream(new File("output.xlsx"))
    val wrkbook: org.apache.poi.ss.usermodel.Workbook = WorkbookFactory.create(file)
    val sheet: org.apache.poi.ss.usermodel.Sheet = wrkbook.getSheetAt(0)
    var rowcount = 0
    var row = sheet.createRow(rowcount)
    row.createCell(0).setCellValue("percent")
    row.createCell(1).setCellValue("stopwords")
    row.createCell(2).setCellValue("stemmer")
    row.createCell(3).setCellValue("feature extraction")
    row.createCell(4).setCellValue("n")
    row.createCell(5).setCellValue("min df")
    row.createCell(6).setCellValue("max df")
    row.createCell(7).setCellValue("freq flag")
    row.createCell(8).setCellValue("classifier")
    row.createCell(9).setCellValue("f1_positive")
    row.createCell(10).setCellValue("f1_negative")
    row.createCell(11).setCellValue("f1_score")
    row.createCell(12).setCellValue("running time")
    rowcount += 1

    for(percent <- percenttraining) {
      println("===================================")
      println("DATA SEPARATING...")
      val data = Preprocessing.datasetSeparator(dataset, percent)
      //data._1 = data train, data._2 = label train, data._3 = data test, data_4 = label test
      for(stopwordFlag <- stopFlag) {
        println("--------------------------------")
        println("Stopwords Removal : " + stopwordFlag)
        for(stemmerFlag <- stemFlag) {
          println("-----------------------------")
          println("Stemmer : " + stemmerFlag)
          var start = System.currentTimeMillis / 1000
          val token = Preprocessing.generateToken(data._1, stopwordFlag, stemmerFlag)
          val token_test = Preprocessing.generateToken(data._3, stopwordFlag, stemmerFlag)
          var end = System.currentTimeMillis / 1000
          val running_time1 = end-start
          for(featxtr <- featurecase) {
            println("----------------------------")
            println("Feature Extraction : " + featxtr)
            featxtr match {
              case "ngram" => {
                for(m <- n) {
                  println("-----------------------")
                  println("N : " + m)
                  for(min <- min_df) {
                    for(max <- max_df) {
                      for(freq <- freqFlag) {
                        println("-------------------")
                        println("Max Freq : " + max)
                        println("Min Freq : " + min)
                        println("Freq Flag : " + freq)
                        var ngram = List[String]()
                        start = System.currentTimeMillis / 1000
                        if(m == 4) {
                          // 1 + 2 gram
                          val ngram1 = FeatureExtraction.generateBag(token,1,min,max).keys.toList
                          val ngram2 = FeatureExtraction.generateBag(token,2,min,max).keys.toList
                          ngram = ngram1 ++ ngram2

                        } else if(m == 5) {
                          //2 + 3 gram
                          val ngram1 = FeatureExtraction.generateBag(token,2,min,max).keys.toList
                          val ngram2 = FeatureExtraction.generateBag(token,3,min,max).keys.toList
                          ngram = ngram1 ++ ngram2
                        } else {
                          ngram = FeatureExtraction.generateBag(token,m,min,max).keys.toList
                        }
                        val filename = "./feature/bag-" + m + "-gram"
                        Preprocessing.saveBag(ngram, filename)
                        val feature = FeatureExtraction.generateNgram(token, ngram, freq)
                        val feature_test = FeatureExtraction.generateNgram(token_test, ngram, freq)
                        end = System.currentTimeMillis / 1000
                        val running_time2 = (end-start)
                        for(classifier <- classifiers) {
                          println("----------------")
                          println("Classifier : " + classifier)
                          val filename = "./model/" + classifier + "-" + featxtr
                          start = System.currentTimeMillis / 1000
                          Classifier.train(classifier, feature, data._2, filename)
                          val result = Classifier.predict(classifier, feature_test, filename)
                          val fscore = PerformanceMeasurement.f1_score(data._4, result)
                          end = System.currentTimeMillis / 1000
                          val running_time = (end-start) + running_time1 + running_time2
                          /*val output = "F-Negative : " + fscore(0)+ "\n" +
                            "F-Positive : " + fscore(1) + "\n" +
                            "F-Score : " + fscore(2) + "\n" +
                            "Running time : " + running_time

                          val pw = new PrintWriter(new File("./result/Performance-" + "-" + percent +
                            "-" + stopFlag + "-" + stemFlag + "-" + featxtr + "-" + m + "-" + min +
                            "-" + max + "-" + freq + "-" + classifier + ".txt"))
                          pw.write(output)
                          pw.close*/

                          //set result to workbook
                          row.createCell(0).setCellValue(percent)
                          row.createCell(1).setCellValue(stopFlag.toString)
                          row.createCell(2).setCellValue(stemFlag.toString)
                          row.createCell(3).setCellValue(featxtr)
                          row.createCell(4).setCellValue(m)
                          row.createCell(5).setCellValue(min)
                          row.createCell(6).setCellValue(max)
                          row.createCell(7).setCellValue(freq)
                          row.createCell(8).setCellValue(classifier)
                          row.createCell(9).setCellValue(fscore(1))
                          row.createCell(10).setCellValue(fscore(0))
                          row.createCell(11).setCellValue(fscore(2))
                          row.createCell(12).setCellValue(running_time)
                          rowcount += 1

                          println("-----------------")
                        }
                        println("-------------------")
                      }
                    }
                  }
                  println("-----------------------")
                }
              }
              case "tfidf" => {
                //create bag of words
                for(m <- n) {
                  println("------------------------")
                  println("N : " + m)
                  for(min <- min_df) {
                    for(max <- max_df) {
                      println("--------------------")
                      println("Min DF : " + min)
                      println("Max DF : " + max)
                      var bag = List[String]()
                      var tf = List[Int]()
                      var sequenced = List[List[String]]()
                      var sequenced_test = List[List[String]]()
                      start = System.currentTimeMillis / 1000
                      if(m == 4) {
                        val tmp1 = FeatureExtraction.generateBag(token,1,min,max)
                        val tmp2 = FeatureExtraction.generateBag(token,2,min,max)
                        bag = tmp1.keys.toList ++ tmp2.keys.toList
                        tf = tmp1.values.toList ++ tmp2.values.toList
                        sequenced = FeatureExtraction.generateSequence(token, 1)
                        sequenced = sequenced ++ FeatureExtraction.generateSequence(token,2)
                        sequenced_test = FeatureExtraction.generateSequence(token_test, 1)
                        sequenced_test = sequenced_test ++ FeatureExtraction.generateSequence(token_test, 2)
                      } else if(m == 5) {
                        //2 + 3 gram
                        val tmp1 = FeatureExtraction.generateBag(token,2,min,max)
                        val tmp2 = FeatureExtraction.generateBag(token,3,min,max)
                        bag = tmp1.keys.toList ++ tmp2.keys.toList
                        tf = tmp1.values.toList ++ tmp2.values.toList
                        sequenced = FeatureExtraction.generateSequence(token, 2)
                        sequenced = sequenced ++ FeatureExtraction.generateSequence(token,3)
                        sequenced_test = FeatureExtraction.generateSequence(token_test, 2)
                        sequenced_test = sequenced_test ++ FeatureExtraction.generateSequence(token_test, 3)
                      } else {
                        val tmp = FeatureExtraction.generateBag(token, m, min, max)
                        bag = tmp.keys.toList
                        tf = tmp.values.toList
                        sequenced = FeatureExtraction.generateSequence(token, m)
                        sequenced_test = FeatureExtraction.generateSequence(token_test, m)
                      }
                      val tfidf = FeatureExtraction.generateTFIDF(tf, data._1.size, bag)
                      val filename = "./feature/tfidf-" + m + "-gram"
                      Preprocessing.saveTFIDF(tfidf, filename)
                      val feature  = FeatureExtraction.tfidfVectorizerAll(sequenced, tfidf, bag)
                      val feature_test  = FeatureExtraction.tfidfVectorizerAll(sequenced_test, tfidf, bag)
                      end = System.currentTimeMillis / 1000
                      val running_time2 = (end-start)
                      for(classifier <- classifiers) {
                        println("----------------")
                        println("Classifier : " + classifier)
                        val filename = "./model/" + classifier + "-" + featxtr
                        start = System.currentTimeMillis / 1000
                        Classifier.train(classifier, feature, data._2, filename)
                        val result = Classifier.predict(classifier, feature_test, filename)
                        val fscore = PerformanceMeasurement.f1_score(data._4, result)
                        end = System.currentTimeMillis / 1000
                        val running_time = (end-start) + running_time1 + running_time2
                        /*val output = "F-Negative : " + fscore(0)+ "\n" +
                          "F-Positive : " + fscore(1) + "\n" +
                          "F-Score : " + fscore(2) + "\n" +
                          "Running time : " + running_time

                        val pw = new PrintWriter(new File("./result/Performance-" + "-" + percent +
                          "-" + stopFlag + "-" + stemFlag + "-" + featxtr + "-" + m + "-" + min +
                          "-" + max + "-" + classifier + ".txt"))
                        pw.write(output)
                        pw.close*/

                        //set result to workbook
                        row.createCell(0).setCellValue(percent)
                        row.createCell(1).setCellValue(stopFlag.toString)
                        row.createCell(2).setCellValue(stemFlag.toString)
                        row.createCell(3).setCellValue(featxtr)
                        row.createCell(4).setCellValue(m)
                        row.createCell(5).setCellValue(min)
                        row.createCell(6).setCellValue(max)
                        row.createCell(7).setCellValue("")
                        row.createCell(8).setCellValue(classifier)
                        row.createCell(9).setCellValue(fscore(1))
                        row.createCell(10).setCellValue(fscore(0))
                        row.createCell(11).setCellValue(fscore(2))
                        row.createCell(12).setCellValue(running_time)
                        rowcount += 1

                        println("-----------------")
                      }
                      println("--------------------")
                    }
                  }
                  println("------------------------")
                }
              }
              case "cfidf" => {

                for(m <- n) {
                  println("------------------------")
                  println("N : " + m)
                  for(min <- min_df) {
                    for(max <- max_df) {
                      println("--------------------")
                      println("Min DF : " + min)
                      println("Max DF : " + max)
                      start = System.currentTimeMillis / 1000

                      val filename = "./feature/dfidf-" + m + "-gram"

                      var sequenced = List[List[String]]()
                      var sequenced_test = List[List[String]]()
                      if(m == 4) {
                        val sequenced1 = token
                        val sequenced2 = FeatureExtraction.generateSequence(token, 2)
                        for(i <- 0 until token.size) {
                          sequenced :+= (sequenced1(i) ::: sequenced2(i))
                        }
                        val sequencedtest1 = token_test
                        val sequencedtest2 = FeatureExtraction.generateSequence(token_test, 2)
                        for(i <- 0 until token_test.size) {
                          sequenced_test :+= (sequencedtest1(i) ::: sequencedtest2(i))
                        }
                      } else if(m == 5) {
                        val sequenced1 = FeatureExtraction.generateSequence(token, 2)
                        val sequenced2 = FeatureExtraction.generateSequence(token, 3)
                        for(i <- 0 until token.size) {
                          sequenced :+= (sequenced1(i) ++ sequenced2(i))
                        }
                        val sequencedtest1 = FeatureExtraction.generateSequence(token_test, 2)
                        val sequencedtest2 = FeatureExtraction.generateSequence(token_test, 3)
                        for(i <- 0 until token_test.size) {
                          sequenced_test :+= (sequencedtest1(i) ++ sequencedtest2(i))
                        }
                      } else if(m == 6) {
                        val sequenced1 = token
                        val sequenced2 = FeatureExtraction.generateSequence(token, 2)
                        val sequenced3 = FeatureExtraction.generateSequence(token, 3)
                        for(i <- 0 until sequenced1.size) {
                          sequenced :+= (sequenced1(i) ::: sequenced2(i) ::: sequenced3(i))
                        }
                        val sequencedtest1 = token_test
                        val sequencedtest2 = FeatureExtraction.generateSequence(token_test, 2)
                        val sequencedtest3 = FeatureExtraction.generateSequence(token_test, 3)
                        for(i <- 0 until sequencedtest1.size) {
                          sequenced_test :+= (sequencedtest1(i) ::: sequencedtest2(i) ::: sequencedtest3(i))
                        }
                      } else {
                        if(m == 1) {
                          sequenced = token
                          sequenced_test = token_test
                        }
                        else {
                          sequenced = FeatureExtraction.generateSequence(token, m)
                          sequenced_test = FeatureExtraction.generateSequence(token_test, m)
                        }
                      }
                      val bag = FeatureExtraction.generateDFIDF(token, data._2, m, min, max)
                      Preprocessing.saveDFIDFClass(bag, filename)
                      val feature = FeatureExtraction.dfidfVectorizerAll(sequenced, bag)
                      val feature_test = FeatureExtraction.dfidfVectorizerAll(sequenced_test, bag)
                      end = System.currentTimeMillis / 1000
                      val running_time2 = (end-start)
                      for(classifier <- classifiers) {
                        println("----------------")
                        println("Classifier : " + classifier)
                        val filename = "./model/" + classifier + "-" + featxtr
                        start = System.currentTimeMillis / 1000
                        Classifier.train(classifier, feature, data._2, filename)
                        val result = Classifier.predict(classifier, feature_test, filename)
                        val fscore = PerformanceMeasurement.f1_score(data._4, result)
                        end = System.currentTimeMillis / 1000
                        val running_time = (end-start) + running_time1 + running_time2
                        /*val output = "F-Negative : " + fscore(0)+ "\n" +
                          "F-Positive : " + fscore(1) + "\n" +
                          "F-Score : " + fscore(2) + "\n" +
                          "Running time : " + running_time

                        val pw = new PrintWriter(new File("./result/Performance-" + "-" + percent +
                          "-" + stopwordFlag + "-" + stemmerFlag + "-" + featxtr + "-" + m + "-" + min +
                          "-" + max + "-" + classifier + ".txt"))
                        pw.write(output)
                        pw.close*/

                        //set result to workbook
                        row.createCell(0).setCellValue(percent)
                        row.createCell(1).setCellValue(stopFlag.toString)
                        row.createCell(2).setCellValue(stemFlag.toString)
                        row.createCell(3).setCellValue(featxtr)
                        row.createCell(4).setCellValue(m)
                        row.createCell(5).setCellValue(min)
                        row.createCell(6).setCellValue(max)
                        row.createCell(7).setCellValue("")
                        row.createCell(8).setCellValue(classifier)
                        row.createCell(9).setCellValue(fscore(1))
                        row.createCell(10).setCellValue(fscore(0))
                        row.createCell(11).setCellValue(fscore(2))
                        row.createCell(12).setCellValue(running_time)
                        rowcount += 1

                        println("-----------------")
                      }
                      println("--------------------")
                    }
                  }
                  println("------------------------")
                }
              }
            }
            println("----------------------------")
          }

          println("-----------------------------")
        }
        println("--------------------------------")
      }
      println("===================================")
    }

  }

  def main(args : Array[String]): Unit = {
    var dataset = (List[String](), List[Int]())
    val datatype = args(0)
    val path = args(1)
    println("LOADING DATA...")
    datatype match {
      case "txt" => dataset = Dataset.loadTxt(path)
      case "xls" => dataset = Dataset.loadExcel(path)
      // catch the default with a variable so you can print it
      case whoa  => println("Unexpected case: " + whoa.toString)
    }

    var stopFlag = Array(true/*,false*/)
    var stemFlag = Array(true/*,false*/)
    var percenttraining = Array(0.5/*, 0.6, 0.7, 0.8*/)
    var featurecase = Array(/*"ngram", "tfidf",*/ "cfidf")
    var n = Array(/*1,2,3,*/4/*,5,6*/)
    var min_df = Array(0/*, 0.001, 0.005, 0.01, 0.05, 0.1*/)
    var max_df = Array(1/*, 0.97, 0.93, 0.9, 0.8, 0.7*/)
    var freqFlag = Array(true, false)
    val classifiers = Array("logistic-regression", "naive-bayes", "random-forest", "decision-tree","svm")
    var max_acc = 0.0
    for(percent <- percenttraining) {
      println("===================================")
      println("DATA SEPARATING...")
      val data = Preprocessing.datasetSeparator(dataset, percent)
      //data._1 = data train, data._2 = label train, data._3 = data test, data_4 = label test
      for(stopwordFlag <- stopFlag) {
        println("--------------------------------")
        println("Stopwords Removal : " + stopwordFlag)
        for(stemmerFlag <- stemFlag) {
          println("-----------------------------")
          println("Stemmer : " + stemmerFlag)
          var start = System.currentTimeMillis / 1000
          val token = Preprocessing.generateToken(data._1, stopwordFlag, stemmerFlag)
          val token_test = Preprocessing.generateToken(data._3, stopwordFlag, stemmerFlag)
          var end = System.currentTimeMillis / 1000
          val running_time1 = end-start
          for(featxtr <- featurecase) {
            println("----------------------------")
            println("Feature Extraction : " + featxtr)
            featxtr match {
              case "ngram" => {
                for(m <- n) {
                  println("-----------------------")
                  println("N : " + m)
                  for(min <- min_df) {
                    for(max <- max_df) {
                      for(freq <- freqFlag) {
                        println("-------------------")
                        println("Max Freq : " + max)
                        println("Min Freq : " + min)
                        println("Freq Flag : " + freq)
                        var ngram = List[String]()
                        start = System.currentTimeMillis / 1000
                        if(m == 4) {
                          // 1 + 2 gram
                          val ngram1 = FeatureExtraction.generateBag(token,1,min,max).keys.toList
                          val ngram2 = FeatureExtraction.generateBag(token,2,min,max).keys.toList
                          ngram = ngram1 ++ ngram2

                        } else if(m == 5) {
                          //2 + 3 gram
                          val ngram1 = FeatureExtraction.generateBag(token,2,min,max).keys.toList
                          val ngram2 = FeatureExtraction.generateBag(token,3,min,max).keys.toList
                          ngram = ngram1 ++ ngram2
                        } else {
                          ngram = FeatureExtraction.generateBag(token,m,min,max).keys.toList
                        }
                        val filename = "./feature/bag-" + m + "-gram"
                        Preprocessing.saveBag(ngram, filename)
                        val feature = FeatureExtraction.generateNgram(token, ngram, freq)
                        val feature_test = FeatureExtraction.generateNgram(token_test, ngram, freq)
                        end = System.currentTimeMillis / 1000
                        val running_time2 = (end-start)
                        for(classifier <- classifiers) {
                          println("----------------")
                          println("Classifier : " + classifier)
                          val filename = "./model/" + classifier + "-" + featxtr
                          start = System.currentTimeMillis / 1000
                          Classifier.train(classifier, feature, data._2, filename)
                          val result = Classifier.predict(classifier, feature_test, filename)
                          val fscore = PerformanceMeasurement.f1_score(data._4, result)
                          end = System.currentTimeMillis / 1000
                          val running_time = (end-start) + running_time1 + running_time2
                          val output = "F-Negative : " + fscore(0)+ "\n" +
                            "F-Positive : " + fscore(1) + "\n" +
                            "F-Score : " + fscore(2) + "\n" +
                            "Running time : " + running_time

                          val pw = new PrintWriter(new File("./result/Performance-" + "-" + percent +
                            "-" + stopFlag + "-" + stemFlag + "-" + featxtr + "-" + m + "-" + min +
                            "-" + max + "-" + freq + "-" + classifier + ".txt"))
                          pw.write(output)
                          pw.close
                          println("-----------------")
                        }
                        println("-------------------")
                      }
                    }
                  }
                  println("-----------------------")
                }
              }
              case "tfidf" => {
              //create bag of words
                for(m <- n) {
                  println("------------------------")
                  println("N : " + m)
                  for(min <- min_df) {
                    for(max <- max_df) {
                      println("--------------------")
                      println("Min DF : " + min)
                      println("Max DF : " + max)
                      var bag = List[String]()
                      var tf = List[Int]()
                      var sequenced = List[List[String]]()
                      var sequenced_test = List[List[String]]()
                      start = System.currentTimeMillis / 1000
                      if(m == 4) {
                        val tmp1 = FeatureExtraction.generateBag(token,1,min,max)
                        val tmp2 = FeatureExtraction.generateBag(token,2,min,max)
                        bag = tmp1.keys.toList ++ tmp2.keys.toList
                        tf = tmp1.values.toList ++ tmp2.values.toList
                        sequenced = FeatureExtraction.generateSequence(token, 1)
                        sequenced = sequenced ++ FeatureExtraction.generateSequence(token,2)
                        sequenced_test = FeatureExtraction.generateSequence(token_test, 1)
                        sequenced_test = sequenced_test ++ FeatureExtraction.generateSequence(token_test, 2)
                      } else if(m == 5) {
                        //2 + 3 gram
                        val tmp1 = FeatureExtraction.generateBag(token,2,min,max)
                        val tmp2 = FeatureExtraction.generateBag(token,3,min,max)
                        bag = tmp1.keys.toList ++ tmp2.keys.toList
                        tf = tmp1.values.toList ++ tmp2.values.toList
                        sequenced = FeatureExtraction.generateSequence(token, 2)
                        sequenced = sequenced ++ FeatureExtraction.generateSequence(token,3)
                        sequenced_test = FeatureExtraction.generateSequence(token_test, 2)
                        sequenced_test = sequenced_test ++ FeatureExtraction.generateSequence(token_test, 3)
                      } else {
                        val tmp = FeatureExtraction.generateBag(token, m, min, max)
                        bag = tmp.keys.toList
                        tf = tmp.values.toList
                        sequenced = FeatureExtraction.generateSequence(token, m)
                        sequenced_test = FeatureExtraction.generateSequence(token_test, m)
                      }
                      val tfidf = FeatureExtraction.generateTFIDF(tf, data._1.size, bag)
                      val filename = "./feature/tfidf-" + m + "-gram"
                      Preprocessing.saveTFIDF(tfidf, filename)
                      val feature  = FeatureExtraction.tfidfVectorizerAll(sequenced, tfidf, bag)
                      val feature_test  = FeatureExtraction.tfidfVectorizerAll(sequenced_test, tfidf, bag)
                      end = System.currentTimeMillis / 1000
                      val running_time2 = (end-start)
                      for(classifier <- classifiers) {
                        println("----------------")
                        println("Classifier : " + classifier)
                        val filename = "./model/" + classifier + "-" + featxtr
                        start = System.currentTimeMillis / 1000
                        Classifier.train(classifier, feature, data._2, filename)
                        val result = Classifier.predict(classifier, feature_test, filename)
                        val fscore = PerformanceMeasurement.f1_score(data._4, result)
                        end = System.currentTimeMillis / 1000
                        val running_time = (end-start) + running_time1 + running_time2
                        val output = "F-Negative : " + fscore(0)+ "\n" +
                          "F-Positive : " + fscore(1) + "\n" +
                          "F-Score : " + fscore(2) + "\n" +
                          "Running time : " + running_time

                        val pw = new PrintWriter(new File("./result/Performance-" + "-" + percent +
                          "-" + stopFlag + "-" + stemFlag + "-" + featxtr + "-" + m + "-" + min +
                          "-" + max + "-" + classifier + ".txt"))
                        pw.write(output)
                        pw.close
                        println("-----------------")
                      }
                      println("--------------------")
                    }
                  }
                  println("------------------------")
                }
              }
              case "cfidf" => {

                for(m <- n) {
                  println("------------------------")
                  println("N : " + m)
                  for(min <- min_df) {
                    for(max <- max_df) {
                      println("--------------------")
                      println("Min DF : " + min)
                      println("Max DF : " + max)
                      start = System.currentTimeMillis / 1000

                      val filename = "./feature/dfidf-" + m + "-gram"

                      var sequenced = List[List[String]]()
                      var sequenced_test = List[List[String]]()
                      if(m == 4) {
                        val sequenced1 = token
                        val sequenced2 = FeatureExtraction.generateSequence(token, 2)
                        for(i <- 0 until token.size) {
                          sequenced :+= (sequenced1(i) ::: sequenced2(i))
                        }
                        val sequencedtest1 = token_test
                        val sequencedtest2 = FeatureExtraction.generateSequence(token_test, 2)
                        for(i <- 0 until token_test.size) {
                          sequenced_test :+= (sequencedtest1(i) ::: sequencedtest2(i))
                        }
                      } else if(m == 5) {
                        val sequenced1 = FeatureExtraction.generateSequence(token, 2)
                        val sequenced2 = FeatureExtraction.generateSequence(token, 3)
                        for(i <- 0 until token.size) {
                          sequenced :+= (sequenced1(i) ++ sequenced2(i))
                        }
                        val sequencedtest1 = FeatureExtraction.generateSequence(token_test, 2)
                        val sequencedtest2 = FeatureExtraction.generateSequence(token_test, 3)
                        for(i <- 0 until token_test.size) {
                          sequenced_test :+= (sequencedtest1(i) ++ sequencedtest2(i))
                        }
                      } else if(m == 6) {
                        val sequenced1 = token
                        val sequenced2 = FeatureExtraction.generateSequence(token, 2)
                        val sequenced3 = FeatureExtraction.generateSequence(token, 3)
                        for(i <- 0 until sequenced1.size) {
                          sequenced :+= (sequenced1(i) ::: sequenced2(i) ::: sequenced3(i))
                        }
                        val sequencedtest1 = token_test
                        val sequencedtest2 = FeatureExtraction.generateSequence(token_test, 2)
                        val sequencedtest3 = FeatureExtraction.generateSequence(token_test, 3)
                        for(i <- 0 until sequencedtest1.size) {
                          sequenced_test :+= (sequencedtest1(i) ::: sequencedtest2(i) ::: sequencedtest3(i))
                        }
                      } else {
                          if(m == 1) {
                            sequenced = token
                            sequenced_test = token_test
                          }
                          else {
                            sequenced = FeatureExtraction.generateSequence(token, m)
                            sequenced_test = FeatureExtraction.generateSequence(token_test, m)
                          }
                      }
                      val bag = FeatureExtraction.generateDFIDF(token, data._2, m, min, max)
                      Preprocessing.saveDFIDFClass(bag, filename)
                      val feature = FeatureExtraction.dfidfVectorizerAll(sequenced, bag)
                      val feature_test = FeatureExtraction.dfidfVectorizerAll(sequenced_test, bag)
                      end = System.currentTimeMillis / 1000
                      val running_time2 = (end-start)
                      for(classifier <- classifiers) {
                        println("----------------")
                        println("Classifier : " + classifier)
                        val filename = "./model/" + classifier + "-" + featxtr
                        start = System.currentTimeMillis / 1000
                        Classifier.train(classifier, feature, data._2, filename)
                        val result = Classifier.predict(classifier, feature_test, filename)
                        val fscore = PerformanceMeasurement.f1_score(data._4, result)
                        end = System.currentTimeMillis / 1000
                        val running_time = (end-start) + running_time1 + running_time2
                        val output = "F-Negative : " + fscore(0)+ "\n" +
                          "F-Positive : " + fscore(1) + "\n" +
                          "F-Score : " + fscore(2) + "\n" +
                          "Running time : " + running_time

                        val pw = new PrintWriter(new File("./result/Performance-" + "-" + percent +
                          "-" + stopwordFlag + "-" + stemmerFlag + "-" + featxtr + "-" + m + "-" + min +
                          "-" + max + "-" + classifier + ".txt"))
                        pw.write(output)
                        pw.close
                        println("-----------------")
                      }
                      println("--------------------")
                    }
                  }
                  println("------------------------")
                }
              }
            }
            println("----------------------------")
          }

          println("-----------------------------")
        }
        println("--------------------------------")
      }
      println("===================================")
    }

  }
}
