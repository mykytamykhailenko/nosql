package com.aimprosoft.phantom.dao

import cats.data.OptionT
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.mongo.connection.Connection
import com.aimprosoft.mongo.dao.{DepartmentDAO, EmployeeDAO}
import com.aimprosoft.mongo.module.PlayModule
import com.aimprosoft.phantom.dao.Util.ScalaMongoContainer
import com.aimprosoft.util.DepartmentExceptions.{DepartmentDoesNotExist, DepartmentIsNotEmpty, DepartmentNameIsAlreadyTaken}
import io.jvm.uuid.UUID
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, BeforeEach}
import org.testcontainers.containers.{GenericContainer, MongoDBContainer}
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}

import java.util.UUID.randomUUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class MongoDAOSpec(implicit ee: ExecutionEnv) extends Specification
  with FutureMatchers
  with BeforeAfterAll
  with BeforeEach {

  // I must have added indexes.
  val mongo =
    new ScalaMongoContainer(DockerImageName.parse("mongo:4.4"))
      .withExposedPorts(27017)
      .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
      .withEnv("MONGO_INITDB_ROOT_PASSWORD", "root")
      .withEnv("MONGO_INITDB_DATABASE", "departments")
      .withCommand("--auth")
      .waitingFor(Wait.forLogMessage("(?i).*Waiting for connections*.*", 1))

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Configuration(
        "mongo.host" -> mongo.getHost,
        "mongo.port" -> mongo.getMappedPort(27017),
        "mongo.user" -> "root",
        "mongo.password" -> "root",
        "mongo.database" -> "admin"))
    .overrides(new PlayModule)
    .build()

  lazy val employees: EmployeeDAO = app.injector.instanceOf[EmployeeDAO]

  lazy val departments: DepartmentDAO = app.injector.instanceOf[DepartmentDAO]

  lazy val connection: Connection = app.injector.instanceOf[Connection]

  def beforeAll(): Unit = mongo.start()

  def afterAll(): Unit = {
    connection.client.close()
    mongo.stop()
  }

  def before(): Unit = connection.truncate()

  "mongo app" should {

    "employee DAO" should {

      "insert an employee with random department" in {
        employees.create(Employee(None, randomUUID(), "Will", "Smith")) must throwA[DepartmentDoesNotExist].await
      }

      "insert an employee with id" in {
        employees.create(Employee(Some(randomUUID()), randomUUID(), "Will", "Smith")) must beNone.await
      }

      "insert an employee and read him by id" in {

        val employee = for {
          dip <- OptionT(departments.create(Department(None, "Agriculture", "")))
          eid <- OptionT(employees.create(Employee(None, dip, "Mike", "")))
          worker <- OptionT(employees.readById(eid))
        } yield worker

        employee.value must beSome[Employee[UUID]]().await
      }

      "insert a few employees and read them" in {

        val workers = for {
          did <- departments.create(Department(None, "Agriculture", ""))

          _ <- employees.create(Employee(None, did.get, "Mike", ""))
          _ <- employees.create(Employee(None, did.get, "Michel", ""))
          _ <- employees.create(Employee(None, did.get, "Ivan", ""))

        } yield employees.getEmployeesByDepartmentId(did.get)

        workers.flatten.map(_.size) must be_===(3).await
      }

      "delete an employee and check if it is not present anywhere" in {

        val deleted = for {

          did <- departments.create(Department[UUID](None, "QA", ""))
          eid <- employees.create(Employee[UUID](None, did.get, "Mike", ""))

          _ <- employees.deleteById(eid.get)

          employeeById <- employees.readById(eid.get)
          employeesByDepartment <- employees.getEmployeesByDepartmentId(did.get)

        } yield employeeById.isEmpty && employeesByDepartment.isEmpty

        deleted must beTrue.await
      }

      "update an employee and change the department, which exist" in {

        val updated = for {

          qa <- departments.create(Department[UUID](None, "QA", ""))
          dev <- departments.create(Department[UUID](None, "Dev", ""))
          eid <- employees.create(Employee[UUID](None, qa.get, "Mike", ""))

          _ <- employees.update(Employee(Some(eid.get), dev.get, "Mike", "Light"))

          // The delete operation must come before the insert. Otherwise this line is None.
          worker <- employees.readById(eid.get)

        } yield worker

        updated must beSome[Employee[UUID]].await
      }

      "update an employee and do not change the department" in {

        val updated = for {

          qa <- departments.create(Department[UUID](None, "QA", ""))
          eid <- employees.create(Employee[UUID](None, qa.get, "Mike", ""))

          _ <- employees.update(Employee(Some(eid.get), qa.get, "Mike", "Light"))

          worker <- employees.readById(eid.get)

        } yield worker

        updated must beSome[Employee[UUID]].await
      }

      "update an employee, who doesn't exist" in {

        val updated = for {
          qa <- departments.create(Department[UUID](None, "QA", ""))
          up <- employees.update(Employee(Some(randomUUID()), qa.get, "Mike", "Light"))
        } yield up

        updated must beSome(0).await
      }

      "update an employee and change department to random department" in {

        val updated = for {

          qa <- departments.create(Department[UUID](None, "QA", ""))
          eid <- employees.create(Employee[UUID](None, qa.get, "Mike", ""))

          up <- employees.update(Employee(Some(eid.get), randomUUID(), "Mike", "Light"))

        } yield up

        updated must throwA[DepartmentDoesNotExist].await
      }
    }

    "department DAO" should {

      "prevent user from creating tow departments with the same name at the same time" in {

        val creation = for {
          _ <- departments.create(Department[UUID](None, "QA", ""))
          _ <- departments.create(Department[UUID](None, "QA", ""))
        } yield ()

        creation must throwA[DepartmentNameIsAlreadyTaken].await
      }

      "free occupied names if the respective department gets deleted" in {

        val division = for {
          id <- departments.create(Department[UUID](None, "QA", "testing"))
          _ <- departments.deleteById(id.get)
          dep <- departments.create(Department[UUID](None, "QA", "test automation"))
        } yield dep

        division must beSome[UUID].await
      }

      "fail to drop a department if it has employees" in {

        val division = for {
          qa <- departments.create(Department[UUID](None, "QA", ""))
          _ <- employees.create(Employee(None, qa.get, "Mike", ""))
          _ <- departments.deleteById(qa.get)
        } yield ()

        division must throwA[DepartmentIsNotEmpty].await
      }

      "fail to update a department if the name is already taken" in {

        val division = for {

          qa <- departments.create(Department[UUID](None, "QA", ""))
          dev <- departments.create(Department[UUID](None, "Dev", ""))

          _ <- departments.update(Department(Some(qa.get), "Dev", ""))

        } yield ()

        division must throwA[DepartmentNameIsAlreadyTaken].await
      }

      "update a department's name (it has not been taken yet)" in {

        val division = for {
          qa <- departments.create(Department[UUID](None, "QA", ""))

          _ <- departments.update(Department(Some(qa.get), "Dev", "testing"))

          div <- departments.readById(qa.get)

        } yield div.map(_.name)

        division must beSome("Dev").await
      }

      "update a department's description" in {

        val division = for {
          qa <- departments.create(Department[UUID](None, "QA", ""))

          _ <- departments.update(Department(Some(qa.get), "QA", "testing"))

          div <- departments.readById(qa.get)

        } yield div

        division must beSome[Department[UUID]].await
      }

      "the departments' names must be freed (you can created another department, which has the same name as the previously updated department)" in {

        val division = for {
          qa <- departments.create(Department[UUID](None, "QA", ""))

          _ <- departments.update(Department(Some(qa.get), "Dev", ""))

          anotherQA <- departments.create(Department[UUID](None, "QA", ""))

          div <- departments.readById(anotherQA.get)

        } yield div

        division must beSome[Department[UUID]].await
      }

    }

  }

}
