package com.aimprosoft.hbase.dao

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

import java.time.Duration

object Util {

  val once = 1

  val zookeeperPort = 2181

  val masterPort = 16000

  val regionServerPort = 16020

  val hbaseWaitDuration: Duration = Duration.ofMinutes(3)

  class ScalaContainer(image: DockerImageName) extends GenericContainer[ScalaContainer](image)

}
