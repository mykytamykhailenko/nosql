package spec

import com.aimprosoft.mat.Materializer
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.service.DepartmentService
import com.aimprosoft.slick.dao.SlickDAO
import com.aimprosoft.slick.mat.SlickMaterializer
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import slickeffect.implicits._

class SlickDepartmentActionSpec(implicit ee: ExecutionEnv) extends SlickSpec with FutureMatchers {

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
  "department service" should {

    implicit val mat: Materializer[DBIO] = SlickMaterializer()

    val service = DepartmentService[DBIO, Int](SlickDAO(departmentTable), SlickDAO(employeeTable))

    "read all employees from the same department" in {
      mat.materialize(service.getEmployeesByDepartmentId(0)) must contain(employees.filter(_.departmentId == 0).toSet).await
    }

  }

}
