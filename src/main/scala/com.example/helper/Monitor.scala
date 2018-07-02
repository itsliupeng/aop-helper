package com.example.helper

import java.util.concurrent.{Executors, ScheduledExecutorService, ThreadPoolExecutor, TimeUnit}

import com.google.common.cache.Cache
import org.slf4j.LoggerFactory

import scala.collection.mutable

/**
  * Created by liupeng on 26/05/2017.
  */
object Monitor {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private var cacheList: mutable.ListMap[String, Cache[_, _]] = mutable.ListMap.empty
  private var threadPoolExecutorList: mutable.ListMap[String, ThreadPoolExecutor] = mutable.ListMap.empty
  private val schedule: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

  schedule.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {
      reportCacheList()
      reportThreadPoolExecutorList()
    }
  }, 0, 30, TimeUnit.SECONDS)

  private[this] def reportCacheList() = cacheList.foreach { case (name, cache) =>
    val statsString = cache.stats().toString
    val hitRate = cache.stats().hitRate()
    logger.info(s"monitor-cache $name: size ${cache.size()} hit_rate ${hitRate}, stats ${statsString}")
  }

  private[this] def reportThreadPoolExecutorList() = threadPoolExecutorList.foreach { case (name, e) =>
    logger.info(s"monitor-threadPool $name: ${e.toString}")
  }

  implicit class CacheMonitor[C <: Cache[_, _]](cache: C) {
    def registerToMonitor(name: String): C = {
      cacheList += (name -> cache)
      cache
    }
  }

  implicit class ThreadPoolExecutorMonitor(e: ThreadPoolExecutor) {
    def registerToMonitor(name: String): ThreadPoolExecutor = {
      threadPoolExecutorList += (name -> e)
      e
    }
  }
}

