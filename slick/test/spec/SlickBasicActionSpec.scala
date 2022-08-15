package spec

import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.{Department, Employee, Id}
import com.aimprosoft.slick.inter.BasicOpSlickInterp.SlickActionLang
import com.aimprosoft.slick.inter.SlickMatLang
import com.aimprosoft.common.lang.MatLang.MatLangOps
import com.aimprosoft.slick.table.{EmployeeTable, departmentTable, employeeTable}
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

    implicit val mat: MatLang[DBIO] = SlickMatLang()

    val lang: BasicActionLang[DBIO, Employee] = SlickActionLang(employeeTable)

    import lang._

    "create an instance without id" in {
      create(Employee(None, 0, "John", "Wick")).materialize() must beSome[Id].await
    }

    "create an instance with id" in {
      create(Employee(Some(42), 0, "John", "Wick")).materialize() must beNone.await
    }

    "update an instance with id" in {
      update(Employee(Some(0), 0, "John", "Wick")).materialize() must beSome[Id](1).await
    }

    "update an instance without id" in {
      update(Employee(None, 0, "John", "Wick")).materialize() must beNone.await
    }

    "read an instance by id" in {
      readById(0).materialize() must beSome(Employee(Some(0), 0, "Tom", "Thomson")).await
    }

    "read all instances" in {
      readAll().materialize() must contain(employees.toSet).await
    }

    "delete an instances by id" in {
      deleteById(0).materialize() must be_===(1).await
    }

    "create, update, and read an instances" in {

      val surname: DBIO[Option[String]] = for {
        id <- create(Employee(None, 0, "John", "the Village"))
        _ <- update(Employee(id, 0, "John", "Marco"))
        employee <- readById(id.get)
      } yield employee.map(_.surname)

      surname.materialize() must beSome[String]("Marco").await
    }

  }


}
