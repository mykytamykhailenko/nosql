package specs

import cats.effect.{Effect, IO}
import com.aimprosoft.common.lang.MatLang.MatLangOps
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.{Department, Employee}
import com.aimprosoft.common.service.EmployeeService
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.inter.DoobieBasicOpInter.DoobieActionLang
import com.aimprosoft.doobie.inter.IOMatLang
import org.specs2.concurrent.ExecutionEnv


class DoobieEmployeeActionSpec(implicit ee: ExecutionEnv) extends DoobieSpec[IO] {

  override implicit def M: Effect[IO] = IO.ioEffect

  override implicit val mat: MatLang[IO] = IOMatLang()

  "employee service" should {

    implicit val mat: MatLang[IO] = IOMatLang()

    val employeeLang: BasicActionLang[IO, Employee] = DoobieActionLang()
    val departmentLang: BasicActionLang[IO, Department] = DoobieActionLang()

    val service = EmployeeService(departmentLang, employeeLang)

    "get an employee with department" in {

      val surname = for {
        scala <- departmentLang.create(Department(None, "Scala", ""))
        id <- employeeLang.create(Employee(None, scala.get, "Tom", "Thomson"))
        employee <- service.getEmployeeById(id.get)
      } yield employee.map(_.surname)

      surname.materialize() must beSome[String]("Thomson").await
    }

  }

}