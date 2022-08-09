package com.aimprosoft

import _root_.slick.basic.DatabaseConfig
import _root_.slick.jdbc.JdbcProfile

package object slick {

  val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("slick.dbs.default")

}
