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
    val percenttraining = Array(0.5, 0.6, 0.7, 0.8)
    val samplesize = (dataset._1.size/2).toInt
    var datatrain = List[String]()
    var labeltrain = List[Int]()
    var datatest = List[String]()
    var labeltest = List[Int]()
    for(percent <- percenttraining) {
      val trainingsize = (0.7 * dataset._1.size/2).toInt
      println("Training : Testing => " + percent + " : "+ "%01.1f".format(1-percent))
      println("Training size : " + trainingsize*2 + " , Testing size : " + (samplesize-trainingsize)*2 )
      println("DATA SEPARATING...")
      for(i <- 0 until samplesize) {
        if(i < trainingsize) {
          datatrain :+= dataset._1(i)
          datatrain :+= dataset._1(i+samplesize)
          labeltrain :+= dataset._2(i)
          labeltrain :+= dataset._2(i+samplesize)
        } else {
          datatest :+= dataset._1(i)
          datatest :+= dataset._1(i+samplesize)
          labeltest :+= dataset._2(i)
          labeltest :+= dataset._2(i+samplesize)
        }
      }

    }

  }
}
