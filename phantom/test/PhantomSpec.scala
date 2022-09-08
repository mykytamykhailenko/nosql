
import Util.{ScalaCassandraContainer, cassandraPort, cassandraWaitDuration, once}
import cats.data.OptionT
import cats.implicits.catsSyntaxApplicativeId
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.phantom.dao.{DepartmentDAO, TEmployeeDAO}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

class PhantomSpec(implicit ee: ExecutionEnv) extends Specification with BeforeAfterAll with FutureMatchers {

  val cassandra: ScalaCassandraContainer =
    new ScalaCassandraContainer(DockerImageName.parse("cassandra:4.0"))
      .withInitScript("divisions.cql")
      .withExposedPorts(cassandraPort)
      .waitingFor(Wait.forLogMessage(".*Created default superuser role 'cassandra'.*", once))
      .withStartupTimeout(cassandraWaitDuration)

  def beforeAll(): Unit = cassandra.start()

  def afterAll(): Unit = cassandra.stop()

  "phantom app" should {

    lazy val app = new GuiceApplicationBuilder()
      .configure(
        Configuration(
          "cassandra.host" -> cassandra.getHost,
          "cassandra.port" -> cassandra.getMappedPort(cassandraPort),
          "cassandra.user" -> cassandra.getUsername,
          "cassandra.password" -> cassandra.getPassword,
          "cassandra.keyspace" -> "divisions"))
      .build()

    lazy val employees = app.injector.instanceOf[TEmployeeDAO]

    lazy val departments = app.injector.instanceOf[DepartmentDAO]

    "insert a department, an employee belonging to the department, and read the employee by id" in {

      val employee = for {
        dip <- OptionT(departments.create(Department(None, "Agriculture", "")))
        eid <- OptionT(employees.create(Employee(None, dip, "Mike", "")))
        worker <- OptionT(employees.readById(eid))
      } yield worker

      employee.value must beSome[Employee[UUID]]().await
    }

    "insert a department, a few employees belonging to the department, and read them" in {

      val dip = for {
        dip <- OptionT(departments.create(Department(None, "Agriculture", "")))

        _ <- OptionT(employees.create(Employee(None, dip, "Mike", "")))
        _ <- OptionT(employees.create(Employee(None, dip, "Michel", "")))
        _ <- OptionT(employees.create(Employee(None, dip, "Ivan", "")))

      } yield dip

      val noEmployees = Seq.empty[Employee[UUID]].pure[Future]

      val workers = for {
        id <- dip.value
      } yield id.fold(noEmployees)(employees.getEmployeesByDepartmentId)

      workers.flatten.map(_.size) must be_===(3).await
    }


  }

}
