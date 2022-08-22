package specs

import cats.effect.{Effect, IO}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.dao.DoobieDAO
import com.aimprosoft.doobie.mat.IOMaterializer
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.service.EmployeeService
import org.specs2.concurrent.ExecutionEnv


class DoobieEmployeeActionSpec(implicit ee: ExecutionEnv) extends DoobieSpec[IO] {

  override implicit def M: Effect[IO] = IO.ioEffect

  override implicit val mat: Materializer[IO] = IOMaterializer()

  "employee service" should {

    implicit val mat: Materializer[IO] = IOMaterializer()

    val employeeLang: BasicDAO[IO, Int, Employee[Int]] = DoobieDAO()
    val departmentLang: BasicDAO[IO, Int, Department[Int]] = DoobieDAO()

    val service = EmployeeService(departmentLang, employeeLang)

    "get an employee with department" in {

      val surname = for {
        scala <- departmentLang.create(Department(None, "Scala", ""))
        id <- employeeLang.create(Employee(None, scala.get, "Tom", "Thomson"))
        employee <- service.getEmployeeById(id.get)
      } yield employee.map(_.surname)

      mat.materialize(surname) must beSome[String]("Thomson").await
    }

  }

}