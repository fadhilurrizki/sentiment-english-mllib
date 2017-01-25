import scala.io.Source

/**
  * Created by fadhilurrizki on 20/01/17.
  */
object Main {
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

    val stopFlag = Array(true/*,false*/)
    val stemFlag = Array(true/*,false*/)
    val percenttraining = Array(/*0.5, 0.6, 0.7,*/ 0.8)
    val featurecase = Array("ngram"/*, "tfidf", "cfidf"*/)
    val n = Array(1/*,2,3*/)
    val min_df = Array(0/*, 0.001, 0.005, 0.01*/)
    val max_df = Array(1/*, 0.9, 0.8, 0.7*/)
    val freqFlag = Array(/*true, */false)

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
          val token = Preprocessing.generateToken(data._1, stopwordFlag, stemmerFlag)
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
                        val ngram = FeatureExtraction.generateBag(token,m,min,max)
                        Preprocessing.saveBag(ngram, m)
                        val feature = FeatureExtraction.generateNgram(token, ngram, freq)
                        println("-------------------")
                      }

                    }
                                      }

                  println("-----------------------")
                }
              }
              case "tfidf" => {

              }
              case "cfidf" => {

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
