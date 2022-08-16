package spec

import com.aimprosoft.common.lang.MatLang
import com.aimprosoft.common.lang.MatLang.MatLangOps
import com.aimprosoft.common.model.{Department, Employee}
import com.aimprosoft.common.service.DepartmentService
import com.aimprosoft.slick.inter.BasicOpSlickInterp.SlickActionLang
import com.aimprosoft.slick.inter.SlickMatLang
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import slickeffect.implicits._

class SlickDepartmentActionSpec(implicit ee: ExecutionEnv) extends SlickSpec with FutureMatchers {

  import dbConfig.profile.api._

  val employees: Seq[Employee] = Seq(
    Employee(Some(0), 0, "Tom", "Thomson"),
    Employee(Some(1), 0, "Jul", "Marko"),
    Employee(Some(2), 1, "Carlo", "Frank"))

  val departments: Seq[Department] = Seq(
    Department(Some(0), "Scala", ""),
    Department(Some(1), "Java", ""))

  override def populateTables: DBIO[Unit] = DBIO.seq(
    departmentTable.forceInsertAll(departments),
    employeeTable.forceInsertAll(employees))

  // I don't think this test is really required.
  "department service" should {

    implicit val mat: MatLang[DBIO] = SlickMatLang()

    val service = DepartmentService(SlickActionLang(departmentTable), SlickActionLang(employeeTable))

    "read all employees from the same department" in {
      service.getEmployeesByDepartmentId(0).materialize() must contain(employees.filter(_.departmentId == 0).toSet).await
    }

  }

}
