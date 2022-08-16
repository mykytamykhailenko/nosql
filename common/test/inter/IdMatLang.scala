package inter

import cats.Id
import com.aimprosoft.common.lang.MatLang

import scala.concurrent.Future

case class IdMatLang() extends MatLang[Id] {

  override def materialize[M](v: Id[M]): Future[M] = Future.successful(v)

}
