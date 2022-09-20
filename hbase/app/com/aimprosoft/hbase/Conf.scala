package com.aimprosoft.hbase

import org.apache.hadoop.conf.{Configuration => HadoopConf}
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants}
import play.api.Configuration

import javax.inject.Inject

class Conf @Inject() (config: Configuration){

  def getHadoopConfig(): HadoopConf = {

    val quorum = config.get[String](HConstants.ZOOKEEPER_QUORUM)

    val masterPort = config.get[Int](HConstants.MASTER_PORT)

    val regionServerPort = config.get[Int]("hbase.regionserver.port")

    val zookeeperClientPort = config.get[Int](HConstants.ZOOKEEPER_CLIENT_PORT)

    val conf = HBaseConfiguration.create()

    conf.setStrings(HConstants.ZOOKEEPER_QUORUM, quorum)

    conf.setInt(HConstants.MASTER_PORT, masterPort)

    conf.setInt("hbase.regionserver.port", regionServerPort)

    conf.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, zookeeperClientPort)

    conf
  }

}
