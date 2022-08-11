package com.aimprosoft.slick.inter

import com.aimprosoft.lang.MatLang
import com.aimprosoft.slick.databaseConfig.profile.api._
import com.aimprosoft.slick.databaseConfig.db

import scala.concurrent.Future

class SlickMatLang extends MatLang[DBIO] {

  def materialize[M](v: DBIO[M]): Future[M] = db.run(v)

}
