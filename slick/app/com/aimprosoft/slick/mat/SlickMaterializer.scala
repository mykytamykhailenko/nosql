package com.aimprosoft.slick.mat

import com.aimprosoft.mat.Materializer
import com.aimprosoft.slick.databaseConfig.db
import com.aimprosoft.slick.databaseConfig.profile.api._

import scala.concurrent.Future

case class SlickMaterializer() extends Materializer[DBIO] {

  def materialize[M](v: DBIO[M]): Future[M] = db.run(v)

}
