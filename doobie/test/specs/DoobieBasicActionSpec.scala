package specs

import cats.effect.{Effect, IO}
import com.aimprosoft.common.lang.MatLang.MatLangOps
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.Department
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.inter.DoobieBasicOpInter.DoobieActionLang
import com.aimprosoft.doobie.inter.IOMatLang
import org.specs2.concurrent.ExecutionEnv

class DoobieBasicActionSpec(implicit ee: ExecutionEnv) extends DoobieSpec[IO] {

  override implicit def M: Effect[IO] = IO.ioEffect

  override implicit val mat: MatLang[IO] = IOMatLang()

  "doobie basic action" should {

    implicit val mat: MatLang[IO] = IOMatLang()

    val lang: BasicActionLang[IO, Department] = DoobieActionLang()

    import lang._

    "create an instance with id and read it" in {
      val name = for {
        id <- create(Department(None, "Scala", ""))
        department <- readById(id.get)
      } yield department.map(_.name)

      name.materialize() must beSome[String]("Scala").await
    }

    "create, update, and read an instance with id and read it" in {
      val name = for {
        id <- create(Department(None, "Scala", ""))
        _ <- update(Department(id, "Java", ""))
        department <- readById(id.get)
      } yield department.map(_.name)

      name.materialize() must beSome[String]("Java").await
    }

    "create an instance without id" in {
      create(Department(Some(42), "Scala", "")).materialize() must beNone.await
    }

    "update an instance without id" in {
      update(Department(None, "Java", "")).materialize() must beNone.await
    }

    "delete an instance" in {
      val all = for {
        id <- create(Department(None, "Scala", ""))
        _ <- deleteById(id.get)
        all <- readAll()
      } yield all

      all.materialize() must be_===(Seq[Department]()).await
    }

  }


}
