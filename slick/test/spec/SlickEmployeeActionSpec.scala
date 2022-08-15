package spec

import com.aimprosoft.common.lang.MatLang.MatLangOps
import com.aimprosoft.common.lang.{BasicActionLang, MatLang}
import com.aimprosoft.common.model.{Department, Employee, EmployeeFull}
import com.aimprosoft.slick.inter.BasicOpSlickInterp.SlickActionLang
import com.aimprosoft.slick.inter.SlickMatLang
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import com.aimprosoft.common.service.EmployeeService
import slickeffect.implicits._

class SlickEmployeeActionSpec(implicit ee: ExecutionEnv) extends SlickSpec with FutureMatchers {

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
  "employee service" should {

    implicit val mat: MatLang[DBIO] = SlickMatLang()

    val service = EmployeeService(SlickActionLang(departmentTable), SlickActionLang(employeeTable))

    "read an employee with his department" in {
      service.getEmployeeById(0).materialize() must beSome[EmployeeFull].await
    }

  }

}
