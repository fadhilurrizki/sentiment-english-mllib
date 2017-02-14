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

  def generateBag(token : List[List[String]], n : Int, min_df : Double = 0, max_df : Double = 1) : Map[String,Int] = {
    val sequence = generateSequence(token, n)
    val flat = sequence.flatten
    val size = flat.size
    flat.groupBy(identity).mapValues(_.size).filter(_._2 < max_df*token.size).filter(_._2 > min_df*token.size)
  }


  def ngramFeaturize(token : List[String], bag : List[String], freq : Boolean = true): List[Double] = {
    val mapp = token.groupBy(identity).mapValues(_.size)
    var result = List[Double]()
    val filtered = mapp.keys.toList.filter(bag.toSet)
    for(i <- 0 until bag.size) {
      var tmpresult = 0
      if(mapp.keys.toList.contains(bag(i))) {
        if(freq)
          tmpresult = mapp.values.toList(mapp.keys.toList.indexOf(bag(i)))
        else
          tmpresult = 1
      }
      result :+= tmpresult.toDouble
    }
    result
  }

  def generateNgram(tokens : List[List[String]], bag : List[String], freq : Boolean = true): List[List[Double]] = {
    var result = List[List[Double]]()
    var a = 0
    for(token <- tokens) {
      result :+= ngramFeaturize(token, bag, freq)
      a += 1
    }
    result
  }

  def generateTFIDF(ngramfeature : List[Int], documentsize : Int, bag : List[String]): List[Double] = {
    var idfrequency = List[Double]()
    var tfidf : List[Double] = ngramfeature map(_.toDouble)
    for(i <- 0 until bag.size ) {
      val filtered = ngramfeature.filter(_ != 0)
      idfrequency :+= Math.log(ngramfeature.size/filtered.size)
      tfidf.foreach(a => a*idfrequency(i))
    }
    tfidf
  }

  def tfidfVectorize(ngram : List[String], tfidf : List[Double], bag : List[String]) : List[Double] = {
    var result = List[Double]()
    for(term <- ngram) {
      if(bag.contains(term))
        result :+= tfidf(bag.indexOf(term))
      else
        result :+= 0.0
    }
    result
  }

  def tfidfVectorizerAll(ngrams : List[List[String]], tfidf : List[Double], bag : List[String]) : List[List[Double]] = {
    var result = List[List[Double]]()
    for(ngram <- ngrams) {
      result :+= tfidfVectorize(ngram, tfidf, bag)
    }
    result
  }

  def generateDFIDF(token : List[List[String]], label : List[Int], n : Int, min_df : Double, max_df : Double) : Map[Int, Map[String, Double]] = {
    /*var negtoken = List[List[String]]()
    var postoken = List[List[String]]()5
    for(i <-0 until token.size) {
      if(label(i) == 0)
        negtoken :+= token(i)
      else
        postoken :+= token(i)
    }*/
    val negtoken = token.zip(label).collect { case (x, 0) => x }
    var negseq = List[List[String]]()
    var bag = Map[String, Int]()
    if(n == 4) {
      val tmp1 = negtoken
      val tmp2 = generateSequence(negtoken,2)
      for(i <- 0 until negtoken.size) {
        negseq :+= (tmp1(i) ::: tmp2(i))
      }
      bag = generateBag(negtoken,1,min_df,max_df) ++ generateBag(negtoken,2,min_df,max_df)
    } else if(n == 5) {
      val tmp1 = generateSequence(negtoken,2)
      val tmp2 = generateSequence(negtoken,3)
      for(i <- 0 until negtoken.size) {
        negseq :+= (tmp1(i) ++ tmp2(i))
      }
      bag = generateBag(negtoken,2,min_df,max_df) ++ generateBag(negtoken,3,min_df,max_df)
    } else if(n == 6) {
      val tmp1 = negtoken
      val tmp2 = generateSequence(negtoken,2)
      val tmp3 = generateSequence(negtoken,3)
      for(i <- 0 until negtoken.size) {
        negseq :+= (tmp1(i) ::: tmp2(i) ::: tmp3(i))
      }
      bag = generateBag(negtoken,1,min_df,max_df) ++ generateBag(negtoken,2,min_df,max_df) ++ generateBag(negtoken,3,min_df,max_df)
    } else {
        negseq = negtoken
        bag = generateBag(negtoken,n,min_df,max_df)
    }
    var result = Map[Int, Map[String, Double]]()
    var tmp = Map[String, Double]()
    for(word <- bag.keys) {
      val filtered = negseq.filter(_.contains(word))
      val df = filtered.size
      if(df != 0) {
        val idf = Math.log(token.size/df)
        val dfidf = df*idf
        tmp += (word -> dfidf)
      }
    }
    val postoken = token.zip(label).collect { case (x, 1) => x }
    var posseq = List[List[String]]()
    var bag2 = Map[String, Int]()
    if(n == 4) {
      val tmp1 = postoken
      val tmp2 = generateSequence(postoken,2)
      for(i <- 0 until postoken.size) {
        posseq :+= (tmp1(i) ::: tmp2(i))
      }
      bag2 = generateBag(postoken,1,min_df,max_df) ++ generateBag(postoken,2,min_df,max_df)
    } else if(n == 5) {
      val tmp1 = generateSequence(postoken,2)
      val tmp2 = generateSequence(postoken,3)
      for(i <- 0 until postoken.size) {
        posseq :+= (tmp1(i) ::: tmp2(i))
      }
      bag2 = generateBag(postoken,2,min_df,max_df) ++ generateBag(postoken,3,min_df,max_df)
    } else if(n == 6) {
      val tmp1 = generateSequence(postoken,1)
      val tmp2 = generateSequence(postoken,2)
      val tmp3 = generateSequence(postoken,3)
      for(i <- 0 until postoken.size) {
        posseq :+= (tmp1(i) ::: tmp2(i) ::: tmp3(i))
      }
      bag2 = generateBag(postoken,1,min_df,max_df) ++ generateBag(postoken,2,min_df,max_df) ++ generateBag(postoken,3,min_df,max_df)
    }else {
      posseq = postoken
      bag2 = generateBag(postoken,n,min_df,max_df)
    }
    var tmp2 = Map[String, Double]()
    for(word <- bag2.keys) {
      val filtered = posseq.filter(_.contains(word))
      val df = filtered.size
      if(df != 0) {
        val idf = Math.log(token.size/df)
        val dfidf = df*idf
        tmp2 += (word -> dfidf)
      }
    }
    Map(0 -> tmp, 1 -> tmp2)
  }

  def dfidfVectorize(token : List[String], bag : Map[Int, Map[String, Double]]) : List[Double] = {
    var result = List[Double]()
    for(elem <- bag) {
      val label = elem._1
      val keywords = elem._2.keys.toList
      val values = elem._2.values.toList
      var tmp = 0.0
      for (tok <- token) {
        if(keywords.contains(tok)) {
          tmp += values(keywords.indexOf(tok))
        }
      }
      result :+= tmp
    }
    result
  }

  def dfidfVectorizerAll(token : List[List[String]], bag : Map[Int, Map[String, Double]]) : List[List[Double]] = {
    var result = List[List[Double]]()
    for(tok <- token) {
      result :+= dfidfVectorize(tok, bag)
    }
    result
  }

}
