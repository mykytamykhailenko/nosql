package com.aimprosoft

import _root_.slick.basic.DatabaseConfig
import _root_.slick.jdbc.JdbcProfile

package object slick {

  // In Play you cannot access injector directly, so you have to reserve to this approach.
  val databaseConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig[JdbcProfile]("slick.dbs.default")

}
