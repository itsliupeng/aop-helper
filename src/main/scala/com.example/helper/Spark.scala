package com.example.helper

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.rdd.RDD

/**
  * Created by liupeng on 18/06/2017.
  */
object Spark {
  implicit class OverwritePath[T <: String](pathStr: String) {
    def overwritePath = {
      val path = new Path(pathStr)
      val fs = path.getFileSystem(new Configuration())
      if (fs.exists(path)) {
        fs.delete(path, true)
      }
      pathStr
    }
  }

  /**
    * "/a/b/c*" will match any file with prefix match c
    * @param pathStr
    * @tparam T
    */
  implicit class RemovePath[T <: String](pathStr: String) {
    import HDFS.filesWithPrefix
    def removePath: Boolean = {
      if (pathStr.endsWith("*")) {
        val dirPathStr = pathStr.slice(0, pathStr.lastIndexOf('/'))
        val filePrefix = pathStr.slice(pathStr.lastIndexOf('/') + 1, pathStr.length - 1)
        val dirPath = new Path(dirPathStr)
        val fs = dirPath.getFileSystem(new Configuration())
        if (fs.exists(dirPath)) {
          val files = filesWithPrefix(dirPath, filePrefix)
          if (files.isEmpty) {
            println(s"cannot find any file matching $pathStr")
            false
          }
          for (file <- files) {
            fs.delete(file.getPath, true)
          }
          true
        } else {
          println(s"directory $dirPathStr not existed")
          false
        }
      } else {
        val path = new Path(pathStr)
        val fs = path.getFileSystem(new Configuration())
        if (fs.exists(path)) {
          fs.delete(path, true)
        } else {
          false
        }
      }
    }
  }


  implicit class SaveAsTextFileAndProduction[T <: RDD[_]](rdd: T) {
    def saveAsTextFileAndProduction(pathStr: String, onlyProduction: Boolean = false) = {
      val productionPath = pathStr.slice(0, pathStr.lastIndexOf('/')) + "/production"
      if (!onlyProduction) {
        rdd.saveAsTextFile(pathStr)
      }
      rdd.saveAsTextFile(productionPath.overwritePath)
    }
  }

  implicit class SaveNotEmptyAsTextFile[T <: RDD[_]](rdd: T) {
    def saveNotEmptyAsTextFile(pathStr: String, isCache: Boolean = true) = {
      if (isCache) rdd.cache()
      if (!rdd.isEmpty()) {
        rdd.saveAsTextFile(pathStr)
      }
    }
  }

  implicit class SaveNotEmptyAsTextFileAndProduction[T <: RDD[_]](rdd: T) {
    def saveNotEmptyAsTextFileAndProduction(pathStr: String, isCache: Boolean = true, onlyProduction: Boolean = false) = {
      if (isCache) rdd.cache()
      if (!rdd.isEmpty()) {
        val productionPath = pathStr.slice(0, pathStr.lastIndexOf('/')) + "/production"
        if (!onlyProduction) {
          rdd.saveAsTextFile(pathStr)
        }
        rdd.saveAsTextFile(productionPath.overwritePath)
      }
    }
  }
}
