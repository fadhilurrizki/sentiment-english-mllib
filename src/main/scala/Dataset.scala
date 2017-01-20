import java.io.{File, FileInputStream}

import org.apache.poi.xssf.usermodel.XSSFWorkbook

import scala.io.Source

/**
  * Created by fadhilurrizki on 20/01/17.
  */
object Dataset {
  def loadTxt(filepath: String): (List[String], List[Int]) = {
    //dataset review polarity, 1000 positive 1000 negative
    var content = List[String]()
    var label = List[Int]()
    //load positive
    val pospath = filepath + "\\pos"
    val posfilenames = getListOfFiles(pospath)
    for(i<-0 to posfilenames.size-1) {
      var text = ""
      for (line <- Source.fromFile(posfilenames(i)).getLines) {
        text += " " + line
      }
      content :+= text
      label :+= 1
    }
    val negpath = filepath + "\\neg"
    val negfilenames = getListOfFiles(negpath)
    for(i<-0 to negfilenames.size-1) {
      var text = ""
      for (line <- Source.fromFile(negfilenames(i)).getLines) {
        text += " " + line
      }
      content :+= text
      label :+= 0
    }
    (content,label)
  }

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  def loadExcel(filepath: String, content_index: Int = 0, label_index: Int = 1): (List[String], List[Int]) = {
    //based on dataset uci amazon
    var content = List[String]()
    var label = List[Int]()
    val filenames = getListOfFiles(filepath)
    for (elem <- filenames) {
      val file = new FileInputStream(new File(elem.toString))
      val workbook = new XSSFWorkbook(file)
      val sheet = workbook.getSheetAt(0)
      val rowStart = sheet.getFirstRowNum()
      val rowEnd = sheet.getLastRowNum
      val row = sheet.getRow(rowStart)
      val cellCount = row.getLastCellNum()
      for(i <- rowStart to rowEnd-1){
        val row = sheet.getRow(i+1)
        var cell = row.getCell(content_index)
        content :+= cell.getStringCellValue
        val cell2 = row.getCell(label_index)
        label :+= cell2.getNumericCellValue.toInt
      }
    }
    (content,label)
  }
}
