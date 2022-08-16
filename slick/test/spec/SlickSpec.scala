package spec

import com.aimprosoft.slick.databaseConfig
import com.aimprosoft.slick.table.{departmentTable, employeeTable}
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterEach, BeforeEach}
import play.api.test.Helpers.{defaultAwaitTimeout, await => awaitAsync}

trait SlickSpec extends Specification with AfterEach with BeforeEach {

  val dbConfig = databaseConfig
  val db = databaseConfig.db

  import dbConfig.profile.api._

  val tables = Seq(employeeTable, departmentTable)

  def populateTables: DBIO[Unit]

  val schema = tables.map(_.schema).reduce(_ ++ _)

  override def before: Unit = {
    val populateDatabase = DBIO.seq(schema.create, populateTables)
    awaitAsync(db.run(populateDatabase))
  }

  override def after: Unit = {
    awaitAsync(db.run(schema.drop))
  }

}
