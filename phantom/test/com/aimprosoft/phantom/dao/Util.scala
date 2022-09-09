package com.aimprosoft.phantom.dao

import org.testcontainers.containers.CassandraContainer
import org.testcontainers.utility.DockerImageName

import java.time.Duration

object Util {

  val once = 1

  val cassandraPort = 9042

  val playPort = 9000

  val cassandraWaitDuration: Duration = Duration.ofMinutes(3)

  class ScalaCassandraContainer(image: DockerImageName) extends CassandraContainer[ScalaCassandraContainer](image)

}
