package com.aimprosoft.hbase.dao

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

import java.time.Duration
import java.util
import scala.jdk.CollectionConverters.MapHasAsJava

object Util {

  val once = 1

  val zookeeperPort = 2181

  val masterPort = 16000

  val regionServerPort = 16020

  val playPort = 9000

  val hbaseWaitDuration: Duration = Duration.ofMinutes(3)

  val hbaseHostname = "hbase"

  val playConf: util.Map[String, String] =
    Map("ZK_QUORUM" -> hbaseHostname,
      "ZK_PORT" -> zookeeperPort.toString,
      "MASTER_PORT" -> masterPort.toString,
      "RS_PORT" -> regionServerPort.toString).asJava

  val hbaseDockerImage: DockerImageName = DockerImageName.parse("dajobe/hbase:latest")

  class ScalaContainer(image: DockerImageName) extends GenericContainer[ScalaContainer](image)

  val employees = "employees"

  val departments = "departments"

  val setUpShName = "/setup.sh"

  val truncateAllShName = "/truncateAll.sh"

  val setUpShBind = "hbase/conf/scripts/setup.sh"

  val truncateAllShBind = "hbase/conf/scripts/truncateAll.sh"

}
