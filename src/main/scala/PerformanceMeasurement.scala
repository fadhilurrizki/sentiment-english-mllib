/**
  * Created by fadhilurrizki on 28/01/17.
  */
object PerformanceMeasurement {
  def f1_score(target : List[Int], output : List[Int], n : Int = 2) : List[Double] = {
    var confusionMatrix = Array.ofDim[Int](n, n)
    var fscoree = List[Double]()
    for (i <- 0 to n - 1) {
      for (j <- 0 to n - 1) {
        confusionMatrix(i)(j) = 0
      }
    }
    for (i <- 0 to target.size - 1) {
      confusionMatrix(output(i))(target(i)) += 1
    }
    var sum_f_score: Double = 0.0
    for (i <- 0 to n - 1) {
      var sum_true_actual: Double = 0.0
      var sum_true_predicted: Double = 0.0
      for (j <- 0 to n - 1) {
        sum_true_actual += confusionMatrix(j)(i)
        sum_true_predicted += confusionMatrix(i)(j)
      }
      val precision: Double = (confusionMatrix(i)(i).toDouble / sum_true_predicted)
      val recall: Double = (confusionMatrix(i)(i).toDouble / sum_true_actual)
      fscoree :+= ((2 * precision * recall) / (precision + recall))
      sum_f_score += ((2 * precision * recall) / (precision + recall))
    }
    fscoree :+= sum_f_score / n
    fscoree
  }
}
