package com.aimprosoft

import cats.effect.kernel.Async
import com.typesafe.config.{Config, ConfigFactory}
import _root_.doobie.Transactor
import _root_.doobie.Transactor.Aux

package object doobie {

  val conf: Config = ConfigFactory.load()

  val driver: String = conf.getString("slick.dbs.default.db.driver")

  val user: String = conf.getString("slick.dbs.default.db.user")

  val password: String = conf.getString("slick.dbs.default.db.password")

  val url: String = conf.getString("slick.dbs.default.db.url")

  implicit def getTransactor[F[_] : Async]: Aux[F, Unit] = Transactor.fromDriverManager[F](driver, url, user, password)

}
