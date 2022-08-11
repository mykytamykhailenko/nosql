package com.aimprosoft

import com.aimprosoft.common.model.{Department, Employee, Id}
import com.typesafe.config.{Config, ConfigFactory}
import _root_.doobie.Transactor
import _root_.doobie.Transactor.Aux
import _root_.doobie.util._
import cats.effect.{Async, ContextShift}

package object doobie {

  val conf: Config = ConfigFactory.load()

  val driver: String = conf.getString("slick.dbs.default.db.driver")

  val user: String = conf.getString("slick.dbs.default.db.user")

  val password: String = conf.getString("slick.dbs.default.db.password")

  val url: String = conf.getString("slick.dbs.default.db.url")

  implicit def getTransactor[F[_] : Async : ContextShift]: Aux[F, Unit] = Transactor.fromDriverManager[F](driver, url, user, password)

  // This doesn't work for Doobie 0.13.4.
  /*
  implicit val departmentWrite: Write[Department] =
    Write[(String, String)].contramap { case Department(_, name, description) =>
      (name, description)
    }

  implicit val employeeWrite: Write[Employee] =
    Write[(Id, String, String)].contramap { case Employee(_, departmentId, name, description) =>
      (departmentId, name, description)
    }*/

  implicit val departmentRead: Read[Department] = Read[(Option[Id], String, String)].map((Department.apply _).tupled)

  implicit val employeeRead: Read[Employee] = Read[(Option[Id], Id, String, String)].map((Employee.apply _).tupled)

}
