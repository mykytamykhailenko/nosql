package com.aimprosoft.common.lang

import scala.concurrent.Future

trait MatLang[F[_]] {

  def materialize[M](v: F[M]): Future[M]

}

object MatLang {

  implicit class MatLangOps[F[_]: MatLang, M](v: F[M]) {

    def materialize(): Future[M] = implicitly[MatLang[F]].materialize(v)

  }

}