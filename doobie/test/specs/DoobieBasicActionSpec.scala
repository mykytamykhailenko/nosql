package specs

import cats.effect.{Effect, IO}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.dao.DoobieDAO
import com.aimprosoft.doobie.mat.IOMaterializer
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.Department
import org.specs2.concurrent.ExecutionEnv

class DoobieBasicActionSpec(implicit ee: ExecutionEnv) extends DoobieSpec[IO] {

  override implicit def M: Effect[IO] = IO.ioEffect

  override implicit val mat: Materializer[IO] = IOMaterializer()

  "doobie basic action" should {

    implicit val mat: Materializer[IO] = IOMaterializer()

    val lang: BasicDAO[IO, Department] = DoobieDAO()

    import lang._

    "create an instance with id and read it" in {
      val name = for {
        id <- create(Department(None, "Scala", ""))
        department <- readById(id.get)
      } yield department.map(_.name)

      mat.materialize(name) must beSome[String]("Scala").await
    }

    "create, update, and read an instance with id and read it" in {
      val name = for {
        id <- create(Department(None, "Scala", ""))
        _ <- update(Department(id, "Java", ""))
        department <- readById(id.get)
      } yield department.map(_.name)

      mat.materialize(name) must beSome[String]("Java").await
    }

    "create an instance without id" in {
      mat.materialize(create(Department(Some(42), "Scala", ""))) must beNone.await
    }

    "update an instance without id" in {
      mat.materialize(update(Department(None, "Java", ""))) must beNone.await
    }

    "delete an instance" in {
      val all = for {
        id <- create(Department(None, "Scala", ""))
        _ <- deleteById(id.get)
        all <- readAll()
      } yield all

      mat.materialize(all) must be_===(Seq[Department]()).await
    }

  }


}
