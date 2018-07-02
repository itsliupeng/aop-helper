package com.example.helper

import java.io.{BufferedReader, InputStreamReader}

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Created by liupeng on 26/05/2017.
  */
object HDFS {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private[this] val fileSystem = FileSystem.get(new Configuration())

  def read[B](pathStr: String)(f: Stream[String] => B): Try[B] = {
    val path = new Path(pathStr)
    if (fileSystem.exists(path)) {
      val files = filesWithPrefix(path).map(p => new BufferedReader(new InputStreamReader(fileSystem.open(p.getPath))))
      val stream = files.map(br => Stream.continually(br.readLine()).takeWhile(_ != null)).foldLeft(Stream.empty[String])(_ ++ _)
      try {
        // f function must be eagerly evaluated before finally
        Success(f(stream))
      } finally {
        files.foreach(f => f.close())
      }
    } else {
      logger.error(s"path $pathStr is not existed")
      Failure(new InvalidPathException(s"path $pathStr is not existed"))
    }
  }

  def filesWithPrefix(path: Path, prefix: String = "part"): Array[FileStatus] = {
    fileSystem.listStatus(path, new PathFilter {
      override def accept(path: Path): Boolean = path.getName.startsWith(prefix)
    })
  }
}
