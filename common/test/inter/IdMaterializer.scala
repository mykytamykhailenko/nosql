package inter

import cats.Id
import com.aimprosoft.mat.Materializer

import scala.concurrent.Future

case class IdMaterializer() extends Materializer[Id] {

  override def materialize[M](v: Id[M]): Future[M] = Future.successful(v)

}
