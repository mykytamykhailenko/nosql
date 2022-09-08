package com.aimprosoft.phantom.connection

import com.outworkers.phantom.dsl._
import play.api.Configuration

import javax.inject.Inject

class Connection @Inject() (conf: Configuration) {

  def getCassandraConnection(): CassandraConnection = {

    val host = conf.get[String]("cassandra.host")
    val port = conf.get[Int]("cassandra.port")

    val password = conf.get[String]("cassandra.password")
    val user = conf.get[String]("cassandra.user")

    val keySpace = conf.get[String]("cassandra.keyspace")

    ContactPoint(host, port).withClusterBuilder(_.withCredentials(user, password)).keySpace(keySpace)
  }


}
