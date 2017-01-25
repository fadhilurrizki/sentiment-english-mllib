/**
  * Created by fadhilurrizki on 24/01/17.
  */
object FeatureExtraction {
  def generateSequence(token : List[List[String]], n : Int) : List[List[String]] = {
    var result = List[List[String]]()
    for(tok <- token) {
      var listtemp = List[String]()
      for(i <- n-1 until tok.size) {
        var tmp = ""
        for(j <- i-n+1 to i) {
          if(tmp == "")
            tmp += tok(j)
          else
            tmp += " " + tok(j)
        }
        listtemp :+= tmp
      }
      result :+= listtemp
    }
    result
  }

  def generateBag(token : List[List[String]], n : Int, min_df : Double = 0, max_df : Double = 1) : List[String] = {
    val sequence = generateSequence(token, n)
    val flat = sequence.flatten
    val size = flat.size
    flat.groupBy(identity).mapValues(_.size).filter(_._2 < max_df*token.size).filter(_._2 > min_df*token.size).keys.toList
  }

  def ngramFeaturize(token : List[String], bag : List[String], freq : Boolean = true): List[Int] = {
    val mapp = token.groupBy(identity).mapValues(_.size)
    var result = List[Int]()
    val filtered = mapp.keys.toList.filter(bag.toSet)
    for(i <- 0 until bag.size) {
      var tmpresult = 0
      if(mapp.keys.toList.contains(bag(i))) {
        if(freq)
          tmpresult = mapp.values.toList(mapp.keys.toList.indexOf(bag(i)))
        else
          tmpresult = 1
      }
      result :+= tmpresult
    }
    result
  }

  def generateNgram(tokens : List[List[String]], bag : List[String], freq : Boolean = true): List[List[Int]] = {
    var result = List[List[Int]]()
    var a = 0
    for(token <- tokens) {
      result :+= ngramFeaturize(token, bag, freq)
      a += 1
    }
    result
  }
}
