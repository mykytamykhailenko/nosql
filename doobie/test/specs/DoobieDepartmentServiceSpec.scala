package specs

import cats.effect.{Effect, IO}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.doobie._
import com.aimprosoft.doobie.dao.DoobieDAO
import com.aimprosoft.doobie.mat.IOMaterializer
import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.service.DepartmentService
import org.specs2.concurrent.ExecutionEnv

class DoobieDepartmentServiceSpec(implicit ee: ExecutionEnv) extends DoobieSpec[IO] {

  override implicit def M: Effect[IO] = IO.ioEffect

  override implicit val mat: Materializer[IO] = IOMaterializer()

  "department service" should {

    implicit val mat: Materializer[IO] = IOMaterializer()

    val employeeLang: BasicDAO[IO, Employee] = DoobieDAO()
    val departmentLang: BasicDAO[IO, Department] = DoobieDAO()

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

      mat.materialize(surnames) must contain("Thomson", "Marko").await
    }

  }

}
