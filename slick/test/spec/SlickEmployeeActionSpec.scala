package spec

import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{CompleteEmployee, Department, Employee}
import com.aimprosoft.service.EmployeeService
import com.aimprosoft.slick.dao.SlickDAO
import com.aimprosoft.slick.mat.SlickMaterializer
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import slickeffect.implicits._

class SlickEmployeeActionSpec(implicit ee: ExecutionEnv) extends SlickSpec with FutureMatchers {

  import dbConfig.profile.api._

  val employees: Seq[Employee[Int]] = Seq(
    Employee(Some(0), 0, "Tom", "Thomson"),
    Employee(Some(1), 0, "Jul", "Marko"),
    Employee(Some(2), 1, "Carlo", "Frank"))

  val departments: Seq[Department[Int]] = Seq(
    Department(Some(0), "Scala", ""),
    Department(Some(1), "Java", ""))

  override def populateTables: DBIO[Unit] = DBIO.seq(
    departmentTable.forceInsertAll(departments),
    employeeTable.forceInsertAll(employees))

  // I don't think this test is really required.
  "employee service" should {

    val mat: Materializer[DBIO] = SlickMaterializer()

    val service = EmployeeService[DBIO, Int](SlickDAO(departmentTable), SlickDAO(employeeTable))

    "read an employee with his department" in {
      mat.materialize(service.getEmployeeById(0)) must beSome[CompleteEmployee[Int]].await
    }

  }

}
