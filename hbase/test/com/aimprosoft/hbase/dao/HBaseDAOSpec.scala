package com.aimprosoft.hbase.dao

import cats.data.OptionT
import Util._
import com.aimprosoft.hbase.AsyncConnectionLifecycle
import com.aimprosoft.model.{Department, Employee}
import com.aimprosoft.util.DepException._
import io.jvm.uuid.UUID
import org.apache.hadoop.conf
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, BeforeEach}
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}

import java.util.UUID.randomUUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class HBaseDAOSpec(implicit ee: ExecutionEnv) extends Specification
  with FutureMatchers
  with BeforeAfterAll
  with BeforeEach {

  /*
  Previously, I wanted to boot up HBase and Play as two Docker containers.

  1. HBase Java client requires the host machine to have the DNS entries for each HBase node.
     In practice, this means:
     1. The host machine must include those entries in /etc/hosts, which cannot be modified so easily:
        1. You must create and remove the entries before and after tests
        2. You must have proper privileges (sudo)

        Fortunately, you can add those entries in Docker container on the fly using --add-host
        https://www.cloudbees.com/blog/using-the-add-host-flag-for-dns-mapping-within-docker-containers

    2. You could start them sequentially:
        1. You would need to start HBase first, get its ports ('getMappedPort') and host ('getHost').
        2. Start the Play app and provide HBase ports and host.
        2. You would need to use WSClient to query the Play app.
    3. You could run both apps in the same network in Docker Compose:
        1. You would need to defined 'depends_on'
        2. You will need to define the waiting strategy
        3. Probably, you won't need to add entries to /etc/hosts because the network provides required DNS entries

  I have found out there is a special class called 'TestingHBaseCluster'.

  1. I won't need to start any containers, which saves a lot of time.
  2. This is a proper solution, which will be easier to use.
   */


  val hbase: ScalaContainer =
    new ScalaContainer(DockerImageName.parse("dajobe/hbase:latest"))
      .withExposedPorts(zookeeperPort, masterPort, regionServerPort)
      .withCreateContainerCmdModifier(cmd => cmd.withHostName("the-cache").add) // I may probably need that
      // .waitingFor(Wait.forHttp("/rest.jsp").forPort(8085).forStatusCode(200))
      .waitingFor(Wait.forLogMessage(".*Finished refreshing block distribution cache.*", once))
      .withStartupTimeout(hbaseWaitDuration)

  // docker run -h hbase -p 2181:2181 -p 16020:16020 -p 16000:16000 dajobe/hbase // WORKS!
  // docker run -h localhost -p 2181:2181 -p 16020:16020 -p 16000:16000 // does not

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Configuration(
        "hbase.zookeeper.quorum" -> hbase.getHost,
        "hbase.zookeeper.property.clientPort" -> hbase.getMappedPort(zookeeperPort),
        "hbase.master.port" -> hbase.getMappedPort(masterPort),
        "hbase.regionserver.port" -> hbase.getMappedPort(regionServerPort))
    )
    .build()

  lazy val employees: EmployeeDAO = app.injector.instanceOf[EmployeeDAO]

  lazy val departments: DepartmentDAO = app.injector.instanceOf[DepartmentDAO]

  lazy val lifeCycle = app.injector.instanceOf[AsyncConnectionLifecycle]


  def beforeAll(): Unit = hbase.start()

  def afterAll(): Unit = hbase.stop()

  def before(): Unit = () // Await.result(lifeCycle.truncate(), 3.seconds)

  "phantom app" should {

    "employee DAO" should {

      "insert an employee with random department" in {
        employees.create(Employee(None, randomUUID(), "Will", "Smith")) must throwA[DepartmentDoesNotExist].await
      }

      "insert an employee with id" in {
        employees.create(Employee(Some(randomUUID()), randomUUID(), "Will", "Smith")) must beNone.await
      }

      "insert an employee and read him by id (check 'employee' table)" in {

        val employee = for {
          dip <- OptionT(departments.create(Department(None, "Agriculture", "")))
          eid <- OptionT(employees.create(Employee(None, dip, "Mike", "")))
          worker <- OptionT(employees.readById(eid))
        } yield worker

        employee.value must beSome[Employee[UUID]]().await
      }

      "insert a few employees and read them (check 'employee_by_department_id' table)" in {

        val workers = for {
          did <- departments.create(Department(None, "Agriculture", ""))

          _ <- employees.create(Employee(None, did.get, "Mike", ""))
          _ <- employees.create(Employee(None, did.get, "Michel", ""))
          _ <- employees.create(Employee(None, did.get, "Ivan", ""))

        } yield employees.getEmployeesByDepartmentId(did.get)

        workers.flatten.map(_.size) must be_===(3).await
      }

      "delete an employee and check if 'employee' and 'employee_by_department_id' do not contain the employee anymore" in {

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
