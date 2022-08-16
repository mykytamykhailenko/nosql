package specs

import cats.effect.{Effect, IO}
import com.aimprosoft.common.lang.MatLang.MatLangOps
import com.aimprosoft.common.lang.{BasicDAO, MatLang}
import com.aimprosoft.common.model.{Department, Employee}
import com.aimprosoft.common.service.DepartmentService
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.inter.DoobieBasicOpInter.DoobieActionLang
import com.aimprosoft.doobie.inter.IOMatLang
import org.specs2.concurrent.ExecutionEnv

class DoobieDepartmentServiceSpec(implicit ee: ExecutionEnv) extends DoobieSpec[IO] {

  override implicit def M: Effect[IO] = IO.ioEffect

  override implicit val mat: MatLang[IO] = IOMatLang()

  "department service" should {

    implicit val mat: MatLang[IO] = IOMatLang()

    val employeeLang: BasicDAO[IO, Employee] = DoobieActionLang()
    val departmentLang: BasicDAO[IO, Department] = DoobieActionLang()

    val service = DepartmentService(departmentLang, employeeLang)

    "get all employees from the same department" in {

      val departmentIds = for {
        scala <- departmentLang.create(Department(None, "Scala", ""))
        java <- departmentLang.create(Department(None, "Java", ""))
      } yield (scala.get, java.get)

      val employeeIds = for {
        (scala, java) <- departmentIds
        _ <- employeeLang.create(Employee(None, scala, "Tom", "Thomson"))
        _ <- employeeLang.create(Employee(None, scala, "Jul", "Marko"))
        _ <- employeeLang.create(Employee(None, java, "Carlo", "Frank"))
      } yield scala

      val surnames = for {
        scala <- employeeIds
        employees <- service.getEmployeesByDepartmentId(scala)
      } yield employees.map(_.surname)

      surnames.materialize() must contain("Thomson", "Marko").await
    }

  }

}
