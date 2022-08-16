package spec

import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Employee, Id}
import com.aimprosoft.slick.dao.SlickDAO
import com.aimprosoft.slick.mat.SlickMaterializer
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers

class SlickBasicActionSpec(implicit ee: ExecutionEnv) extends SlickSpec with FutureMatchers {

  import dbConfig.profile.api._

  val employees: Seq[Employee] = Seq(
    Employee(Some(0), 0, "Tom", "Thomson"),
    Employee(Some(1), 0, "Jul", "Marko"))

  val departments: Seq[Department] = Seq(
    Department(Some(0), "Scala", ""))

  override def populateTables: DBIO[Unit] = DBIO.seq(
    departmentTable.forceInsertAll(departments),
    employeeTable.forceInsertAll(employees))

  "slick basic action" should {

    implicit val mat: Materializer[DBIO] = SlickMaterializer()

    val lang: BasicDAO[DBIO, Employee] = SlickDAO(employeeTable)

    import lang._

    "create an instance without id" in {
      mat.materialize(create(Employee(None, 0, "John", "Wick"))) must beSome[Id].await
    }

    "create an instance with id" in {
      mat.materialize(create(Employee(Some(42), 0, "John", "Wick"))) must beNone.await
    }

    "update an instance with id" in {
      mat.materialize(update(Employee(Some(0), 0, "John", "Wick"))) must beSome[Id](1).await
    }

    "update an instance without id" in {
      mat.materialize(update(Employee(None, 0, "John", "Wick"))) must beNone.await
    }

    "read an instance by id" in {
      mat.materialize(readById(0)) must beSome(Employee(Some(0), 0, "Tom", "Thomson")).await
    }

    "read all instances" in {
      mat.materialize(readAll()) must contain(employees.toSet).await
    }

    "delete an instances by id" in {
      mat.materialize(deleteById(0)) must be_===(1).await
    }

    "create, update, and read an instances" in {

      val surname: DBIO[Option[String]] = for {
        id <- create(Employee(None, 0, "John", "the Village"))
        _ <- update(Employee(id, 0, "John", "Marco"))
        employee <- readById(id.get)
      } yield employee.map(_.surname)

      mat.materialize(surname) must beSome[String]("Marco").await
    }

  }


}
