package com.aimprosoft.hbase.dao

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.hbase.dao.ClientUtil.RequestOps
import com.aimprosoft.hbase.dao.Util._
import com.aimprosoft.model.{Department, Employee}
import io.jvm.uuid.UUID
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterEach, BeforeAfterAll}
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.{DockerImageName, MountableFile}
import play.api.libs.json.{JsNull, JsValue}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.duration.DurationInt


class HBaseDAOSpec(implicit ee: ExecutionEnv) extends Specification
  with FutureMatchers
  with BeforeAfterAll
  with AfterEach {

  /*
  I have found out there is a special class for testing HBase called 'TestingHBaseCluster'.
  I have decided not to use 'TestingHBaseCluster' because of complex conflict resolution problems:
   1. Play uses the most recent Guava version '31.0-jre', while HBase client needs '25.0-jre' at most.
   2. Even if you force downgrade Guava version, you will encounter with significant issues with Servlets.
   3. Upgrading Hadoop to Hadoop 3 solves problems with Guava, but introduces new ones.
   4. Upgrading HBase client to '3.0.0-alpha-3' does not help neither (you will still encounter errors that some methods are not found)

   Note: Intellij successfully runs tests, but sbt does not.
   */

  implicit val system = ActorSystem()

  implicit val materializer = ActorMaterializer()

  val client: StandaloneAhcWSClient = StandaloneAhcWSClient()

  val net: Network = Network.newNetwork()

  val hbase: ScalaContainer =
    new ScalaContainer(hbaseDockerImage)
      .withNetwork(net)
      .withNetworkAliases(hbaseHostname)
      .withCreateContainerCmdModifier(modifier => modifier.withHostName(hbaseHostname))
      .waitingFor(Wait.forLogMessage(".*Finished refreshing block distribution cache.*", once))
      .withStartupTimeout(hbaseWaitDuration)
      .withCopyFileToContainer(MountableFile.forHostPath(setUpShBind, 700), setUpShName)
      .withCopyFileToContainer(MountableFile.forHostPath(truncateAllShBind, 700), truncateAllShName)

  val app: ScalaContainer =
    new ScalaContainer(DockerImageName.parse("playapp:hbase")) // I will later build everything from scratch
      .dependsOn(hbase)
      .withNetwork(net)
      .withExposedPorts(playPort)
      .withEnv(playConf)
      .waitingFor(Wait.forHttp("/health"))

  lazy val host: String = app.getHost

  lazy val port: Int = app.getMappedPort(playPort)

  def beforeAll(): Unit = {
    hbase.start()
    hbase.execInContainer(setUpShName)
    app.start()
  }

  def afterAll(): Unit = {
    app.stop()
    hbase.stop()
  }

  def after(): Unit = hbase.execInContainer(truncateAllShName)

  lazy val helpers: ClientUtil = ClientUtil(client, host, port)

  import helpers._

  // This is a must!
  sequential

  "hbase app" should {

    "fail to insert an employee with random department" in {
      createEmployee(UUID.random, "Will", "Smith")
        .map(response => (response \ "message").as[String]) must be.await
    }

    "refuse to insert an employee with random id" in {
      client.url(createUrl(employees))
        .create(Employee(UUID.random.some, UUID.random, "Will", "Smith")) must be_===[JsValue](JsNull).await
    }

    "refuse to insert a department with random id" in {
      client.url(createUrl(departments))
        .create(Department(UUID.random.some, "Scala", "")) must be_===[JsValue](JsNull).await
    }

    "insert a department" in {
      createDepartment("Scala", "").map(_.asOpt[UUID]) must beSome[UUID].await
    }

    "read a department" in {

      val dep = for {
        id <- createDepartment("Scala", "").map(_.as[UUID])
        dep <- readDepartmentById(id)
      } yield dep.asOpt[Department[UUID]]

      dep must beSome[Department[UUID]]().await
    }

    "disallow inserting two departments with the same name" in {

      val fullStack = for {
        _ <- createDepartment("Scala", "big data")
        dep <- createDepartment("Scala", "full stack")
      } yield dep

      fullStack.map(response => (response \ "message").as[String]) must contain("Scala").await
    }

    "read all departments" in {

      val departments = for {
        _ <- createDepartment("Scala", "")
        _ <- createDepartment("Java", "")
        divisions <- readAllDepartments()
      } yield divisions.as[Seq[Department[UUID]]]

      departments.map(_.size) must be_===(2).await
    }

    "delete a department and create it again" in {

      // When the department gets deleted, its name must be deleted too.
      val department = for {
        id <- createDepartment("Scala", "")
        _ <- deleteDepartmentById(id.as[UUID])
        division <- createDepartment("Scala", "")
      } yield division.asOpt[UUID]

      department must beSome[UUID]().await

    }

    "fail to delete a department with employees" in {

      val deletion = for {
        id <- createDepartment("Scala", "")
        _ <- createEmployee(id.as[UUID], "John", "")
        drop <- deleteDepartmentById(id.as[UUID])
      } yield drop

      deletion.map(response => (response \ "message").as[String]) must contain("is not empty").await
    }

    "it must be impossible to create an employee belonging to a deleted department" in {

      val creation = for {
        id <- createDepartment("Scala", "")
        _ <- deleteDepartmentById(id.as[UUID])
        create <- createEmployee(id.as[UUID], "John", "")
      } yield create

      creation.map(response => (response \ "message").as[String]) must contain("does not exist").await
    }

    "the department must not change its name to a name, which is already taken" in {

      val updated = for {
        id <- createDepartment("Java", "")
        _ <- createDepartment("Scala", "")
        up <- updateDepartment(Department(id.asOpt[UUID], "Scala", ""))
      } yield up

      updated.map(response => (response \ "message").as[String]) must contain("is already taken").await


    }

    "the department must update description only" in {

      val desc = for {
        id <- createDepartment("Java", "")
        _ <- updateDepartment(Department(id.asOpt[UUID], "Java", "Liferay solutions"))
        dep <- readDepartmentById(id.as[UUID])
      } yield dep.as[Department[UUID]].description


      desc must be_===("Liferay solutions").await
    }

    "update the department while freeing space" in {

      val desc = for {
        id <- createDepartment("Java", "")
        _ <- updateDepartment(Department(id.asOpt[UUID], "Scala", ""))
        dep <- createDepartment("Java", "")
      } yield dep


      desc.map(_.asOpt[UUID]) must beSome[UUID].await
    }

    "update nothing because the department does not exists" in {
      updateDepartment(Department(UUID.random.some, "Scala", "")).map(_.as[Int]) must be_===(0).await
    }

    "read all employees by department" in {

      val employees = for {
        scala <- createDepartment("Scala", "")
        java <- createDepartment("Java", "")

        _ <- createEmployee(scala.as[UUID], "Mike", "")
        _ <- createEmployee(scala.as[UUID], "John", "")
        _ <- createEmployee(java.as[UUID], "Michel", "")

        workers <- readEmployeesByDepartmentId(scala.as[UUID])
      } yield workers.as[Seq[Employee[UUID]]].map(_.name)

      employees must contain("Mike", "John").await
    }

    "deleted employees should not be found by their department" in {

      val departmentId = for {
        scala <- createDepartment("Scala", "")
        id <- createEmployee(scala.as[UUID], "Mike", "")
        _ <- deleteEmployeeById(id.as[UUID])
      } yield scala.as[UUID]

      val workerByDepId = for {
        id <- departmentId
        workerByDepId <- readEmployeesByDepartmentId(id)
      } yield workerByDepId.as[Seq[Employee[UUID]]]

      val workerById = for {
        id <- departmentId
        workerById <- readEmployeeById(id)
      } yield workerById.asOpt[Employee[UUID]]

      workerByDepId must be_===(Seq[Employee[UUID]]()).await
      workerById must beNone.await
    }

    "update employees name and surname without changing department" in {

      val ids = for {
        scala <- createDepartment("Scala", "")
        id <- createEmployee(scala.as[UUID], "Mike", "Mike")
      } yield scala.as[UUID] -> id.as[UUID]

      val changed = for {
        (scala, worker) <- ids
        _ <- updateEmployee(Employee(worker.some, scala, "Shon", "Shon"))
      } yield ()

      val workerById = for {
        (_, worker) <- ids
        _ <- changed
        workerById <- readEmployeeById(worker)
      } yield workerById.asOpt[Employee[UUID]].map(_.name)

      val workerByDepId = for {
        (scala, _) <- ids
        _ <- changed
        workerByDepId <- readEmployeesByDepartmentId(scala)
      } yield workerByDepId.as[Seq[Employee[UUID]]].map(_.name)

      workerById must beSome("Shon").awaitFor(10.seconds)
      workerByDepId must be_===(Seq("Shon")).awaitFor(10.seconds)
    }

    "update employee while changing department" in {
      val ids = for {
        scala <- createDepartment("Scala", "")
        java <- createDepartment("Java", "")
        id <- createEmployee(scala.as[UUID], "Mike", "Mike")
      } yield (scala.as[UUID], java.as[UUID], id.as[UUID])

      val changed = for {
        (_, java, worker) <- ids
        _ <- updateEmployee(Employee(worker.some, java, "Shon", "Shon"))
      } yield ()


      val workerById = for {
        (_, _, worker) <- ids
        _ <- changed
        workerById <- readEmployeeById(worker)
      } yield workerById.asOpt[Employee[UUID]].map(_.name)

      val workerByDepId = for {
        (_, java, _) <- ids
        _ <- changed
        workerByDepId <- readEmployeesByDepartmentId(java)
      } yield workerByDepId.as[Seq[Employee[UUID]]].map(_.name)

      val workerByPreviousDepId = for {
        (scala, _, _) <- ids
        _ <- changed
        workerByDepId <- readEmployeesByDepartmentId(scala)
      } yield workerByDepId.asOpt[Employee[UUID]]

      workerById must beSome("Shon").await
      workerByDepId must be_===(Seq("Shon")).await
      workerByPreviousDepId must beNone.await
    }


  }

}
