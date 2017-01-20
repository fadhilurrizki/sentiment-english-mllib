/**
  * Created by fadhilurrizki on 20/01/17.
  */
object Main {
  def main(args : Array[String]): Unit = {
    var dataset = (List[String](), List[Int]())
    args(0) match {
      case "txt" => dataset = Dataset.loadTxt(args(1))
      case "xls" => dataset = Dataset.loadExcel(args(1))
      // catch the default with a variable so you can print it
      case whoa  => println("Unexpected case: " + whoa.toString)
    }
    for(i<-0 until 10) {
      println(dataset._1(i))
      println(dataset._2(i))
    }
    println(dataset._1.size)
    println(dataset._2.size)
  }
}
