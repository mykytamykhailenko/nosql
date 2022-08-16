package specs

import cats.effect.ContextShift
import cats.implicits.toTraverseOps
import com.aimprosoft.common.lang.MatLang
import com.aimprosoft.common.lang.MatLang.MatLangOps
import com.aimprosoft.doobie.getTransactor
import doobie.Transactor
import doobie.implicits.toSqlInterpolator
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterEach, BeforeEach}
import play.api.test.Helpers.{defaultAwaitTimeout, await => awaitAsync}

// Do not need to provide Async, because it has already git Effect.
abstract class DoobieSpec[F[_] : ContextShift] extends Specification with AfterEach with BeforeEach with FutureMatchers with doobie.specs2.Checker[F] {

  def transactor: Transactor[F] = getTransactor[F]

  implicit val mat: MatLang[F]

  override def before: Unit = {
    val employees =
      (fr"CREATE TABLE `employees` (" ++
        fr"`employee_id` int NOT NULL AUTO_INCREMENT," ++
        fr"`department_id` int DEFAULT NULL," ++
        fr"`name` varchar(64) DEFAULT NULL," ++
        fr"`surname` varchar(64) DEFAULT NULL," ++
        fr"`age` int DEFAULT NULL," ++
        fr"PRIMARY KEY (`employee_id`)," ++
        fr"KEY `emp_to_dep` (`department_id`)," ++
        fr"KEY `employee_ix` (`name`,`surname`)," ++
        fr"CONSTRAINT `emp_to_dep` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)" ++
        fr")").update.run

    val departments =
      (fr"CREATE TABLE `departments` (" ++
        fr"`department_id` int NOT NULL AUTO_INCREMENT," ++
        fr"`name` varchar(256) DEFAULT NULL," ++
        fr"`description` varchar(512) DEFAULT NULL," ++
        fr"PRIMARY KEY (`department_id`)," ++
        fr"KEY `department_ix` (`name`)" ++
        fr")").update.run

    val tables = Seq(departments, employees).sequence
    val create = transactor.trans.apply(tables).materialize()

    awaitAsync(create)
  }

  override def after: Unit = {
    val employees = sql"DROP TABLE `employees`".update.run
    val departments = sql"DROP TABLE `departments`".update.run

    val tables = Seq(employees, departments).sequence
    val delete = transactor.trans.apply(tables).materialize()

    awaitAsync(delete)
  }


}
