package com.aimprosoft.hbase.dao

import akka.actor.{ActorSystem, Scheduler}
import cats.implicits.{catsStdInstancesForFuture, catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.hbase.AsyncConnectionLifecycle
import com.aimprosoft.hbase.Util._
import com.aimprosoft.model.{Affected, Department}
import com.aimprosoft.util.DepException.DepartmentNameIsAlreadyTaken
import com.google.inject.Inject
import io.jvm.uuid._
import org.apache.hadoop.hbase.client.{AsyncConnection, Delete, Result, Scan}
import org.apache.hadoop.hbase.util.Bytes.toBytes

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.jdk.FutureConverters.CompletionStageOps

@Singleton
class DepartmentDAO @Inject()(connectionLifecycle: AsyncConnectionLifecycle, system: ActorSystem)(implicit ec: ExecutionContext) extends BasicDAO[Future, UUID, Department[UUID]] {

  private[this] implicit val scheduler: Scheduler = system.scheduler

  import connectionLifecycle._

  private[dao] def usingBothTables[T](calc: ((AsyncTable, AsyncTable)) => Future[T]): Future[T] = {
    val res = for {
      dep <- departments
      name <- names
    } yield calc((dep, name))

    res.flatten
  }


  /**
   * Creates the department and rolls back any changes in case of failure.
   *
   * The creation of the department entails:
   *  1. Insertion into ''department''
   *  1. Insertion into ''department_name''
   *
   * This method does not check the department's name uniqueness, so be careful.
   *
   * This method can reapply failed insertions:
   *  1. It captures the time of the insertion
   *  1. It tries to insert the department
   *  1. If either ''department'' or ''department_name'' fail, it tries to reapply changes using the captured time.
   *
   * @param departments The departments table
   * @param names       The names table
   * @param division    The department
   * @return The department's UUID
   */
  private[dao] def createDivisionOrReapply(departments: AsyncTable, names: AsyncTable, division: Department[UUID]): Future[Option[UUID]] = {

    val currentTime = System.currentTimeMillis() // Capture time for proper rollbacks.

    val putDep = createPutForDepartment(division, currentTime)

    val putName = createPutForName(division, currentTime)

    def createDep = departments.put(putDep).asScala

    val createDepAndReapply = createDep.recoverWith(_ => retries(createDep _))

    def createName = names.put(putName).asScala

    val createNameAndReapply = createName.recoverWith(_ => retries(createName _))

    createDepAndReapply.zip(createNameAndReapply).map(_ => division.id)
  }

  /**
   * Creates the department if its name is unique.
   *
   * @param departments The department table
   * @param names       The department name table
   * @param division    The department
   * @return The department's UUID
   */
  private[dao] def createDivision(departments: AsyncTable, names: AsyncTable, division: Department[UUID]): Future[Option[UUID]] = {

    val creation = for {
      occupied <- names.exists(createGet(toBytes(division.name))).asScala
    } yield if (occupied) Future.failed(DepartmentNameIsAlreadyTaken(division.name))
    else createDivisionOrReapply(departments, names, division)

    creation.flatten
  }


  /**
   * Creates a department with unique name and returns its UUID.
   *
   * The department must not contain the UUID, it will be created for you.
   * This method won't create the department if it has the UUID.
   *
   * If the department's name is duplicate, this method will break.
   *
   * This method can reapply failed transactions.
   *
   * @param division The department.
   * @return The department's UUID
   */
  /*
  Previously, I tried to roll back fail transactions, but this approach had a few flaws:
  1) I could not roll back tombstones when I needed to drop a few records at a time.
     I only could reapply them until they take effect.
  2) I had to ensure that all actions are rolled back at the same time.
     Sometimes, it is not possible.

  Both approaches have a window of inconsistent state, but the approach where I reapply mutations does not suffer
  from these drawbacks and is generally easier.
   */
  override def create(division: Department[UUID]): Future[Option[UUID]] = usingBothTables { case (departments, names) =>

    division.id.fold {

      createDivision(departments, names, division.copy(id = UUID.random.some))

    }(_ => none[UUID].pure[Future])

  }

  /**
   * This method is used to update the department's description only.
   *
   * This method can roll back the update in case of failure.
   *
   * @param departments The department table
   * @param dep         The department
   * @return Always Some(1)
   */
  private[dao] def updateDescription(departments: AsyncTable, dep: Department[UUID]): Future[Option[Affected]] = {

    val currentTime = System.currentTimeMillis()

    val put = createPutForDesc(dep, currentTime)

    departments
      .put(put)
      .asScala
      .recoverWith(_ => rollbackByKey(departments, put.getRow, currentTime))
      .map(_ => one.some)
  }

  private[dao] def foo(departments: AsyncTable, dep: Department[UUID]): Future[Option[Affected]] = {

    val currentTime = System.currentTimeMillis()

    val putDep = createPut(dep.id.get.byteArray, dpBytes, currentTime, Map(
      nameBytes -> toBytes(dep.name),
      descBytes -> toBytes(dep.description)
    ))

    departments
      .put(putDep)
      .asScala
      .recoverWith(_ => rollbackByKey(departments, putDep.getRow, currentTime))
      .map(_ => one.some)
  }

  private[dao] def updateDivisionForFreeName(departments: AsyncTable,
                                             names: AsyncTable,
                                             division: Result,
                                             name: Result,
                                             dep: Department[UUID]): Future[Option[Affected]] = {


    val noEffect = unaffected.some.pure[Future]

    if (division.isEmpty) noEffect else {


      Option(name.value).fold {

        for {
          _ <- deleteById(dep.id.get)
          _ <- createDivisionForFreeName(departments, names, dep)
        } yield one.some

      } { id =>

        if (UUID(division.getRow) == UUID(id)) updateDescription(departments, dep)
        else Future.failed(DepartmentNameIsAlreadyTaken(byteArrayToString(name.getRow)))
      }

    }

  }

  override def update(dep: Department[UUID]): Future[Option[Affected]] = usingBothTables { case (departments, names) =>

    dep.id.fold(none[Affected].pure[Future]) { id =>

      val v = for {
        division <- departments.get(createGet(id.byteArray)).asScala
        name <- names.get(createGet(toBytes(dep.name))).asScala
      } yield updateDivisionForFreeName(departments, names, division, name, dep)

      v.flatten
    }

  }

  override def readAll(): Future[Seq[Department[UUID]]] = usingBothTables { case (departments, _) =>

    for {
      divisions <- departments.scanAll(new Scan()).asScala
    } yield for {
      div <- divisions.asScala.toSeq
    } yield resultToDepartment(div)

  }

  override def readById(id: UUID): Future[Option[Department[UUID]]] = usingBothTables { case (departments, _) =>

    for {
      division <- departments.get(createGet(id.byteArray)).asScala
    } yield if (!division.isEmpty) resultToDepartment(division).some else None

  }

  private[dao] def deleteAndRetry(departments: AsyncTable, names: AsyncTable, division: Result) =
    if (!division.isEmpty) {

      val currentTime = System.currentTimeMillis()

      val divisionNameBytes = division.getValue(dpBytes, nameBytes)

      val dropDivision = retries(() => departments.delete(new Delete(division.getRow).setTimestamp(currentTime)).asScala)

      val dropName = retries(() => names.delete(new Delete(divisionNameBytes).setTimestamp(currentTime)).asScala)

      dropDivision.zip(dropName).map(_ => one)
    }
    else Future.successful(unaffected)

  override def deleteById(id: UUID): Future[Affected] = usingBothTables { case (departments, names) =>

    val drop = for {
      division <- departments.get(createGet(id.byteArray)).asScala
    } yield deleteAndRetry(departments, names, division)

    drop.flatten
  }

}