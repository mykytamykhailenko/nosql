package com.aimprosoft.hbase.dao

import akka.actor.Scheduler
import akka.dispatch.Dispatcher
import cats.implicits.{catsStdInstancesForFuture, catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.hbase.AsyncConnectionLifecycle
import com.aimprosoft.hbase.Util._
import com.aimprosoft.model.{Affected, Department}
import com.google.inject.Inject
import io.jvm.uuid.StaticUUID.random
import io.jvm.uuid._
import org.apache.hadoop.hbase.TableName.{valueOf => tableName}
import org.apache.hadoop.hbase.client.{AdvancedScanResultConsumer, AsyncConnection, Connection, Delete, Get, Table}
import org.apache.hadoop.hbase.util.Bytes

import javax.inject.Singleton
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.jdk.FutureConverters.CompletionStageOps
import akka.pattern.retry
import com.aimprosoft.util.DepException.DepartmentNameIsAlreadyTaken

import scala.jdk.CollectionConverters.IterableHasAsScala

@Singleton
class DepartmentDAO @Inject()(connectionLifecycle: AsyncConnectionLifecycle)(implicit dispatcher: Dispatcher, scheduler: Scheduler) extends BasicDAO[Future, UUID, Department[UUID]] {

  private[dao] val connection: Future[AsyncConnection] = connectionLifecycle.connection

  private[dao] val departments = connection.map(_.getTable(depTableName))

  private[dao] val departmentNames = connection.map(_.getTable(depNameTableName))

  private[dao] def usingBothTables[T](calc: ((AsyncTable, AsyncTable)) => Future[T]): Future[T] = {
    val res = for {
      dep <- departments
      name <- departmentNames
    } yield calc((dep, name))

    res.flatten
  }

  private[dao] def createDivisionForBytes(departments: AsyncTable,
                                          names: AsyncTable,
                                          divisionKeyBytes: ByteArray,
                                          divisionNameBytes: ByteArray,
                                          divisionIdBytes: ByteArray) = {

    val millis = System.currentTimeMillis() // Capture time for proper rollbacks.

    val putDepRequest = createEmptyPut(divisionKeyBytes, depFamilyBytes, millis)
    val putNameRequest = createPut(divisionNameBytes, depFamilyBytes, millis, divisionIdBytes)

    val putDiv = departments.put(putDepRequest).asScala.recoverWith { error =>
      rollbackByKey(departments, divisionKeyBytes, millis, error)
      Future.failed(error) // Both actions must be reverted at the same time.
    }

    val putName = names.put(putNameRequest).asScala.recoverWith { error =>
      rollbackByKey(names, divisionNameBytes, millis, error)
      Future.failed(error)
    }

    putDiv.zip(putName)
  }

  private[dao] def prepareAndCreateDivision(departments: AsyncTable, names: AsyncTable, division: Department[UUID]): Future[Option[UUID]] = {

    val id = UUID.random

    val divisionWithId = division.copy(id = id.some)

    val divisionKeyBytes = buildDivisionKeyAsByteArray(divisionWithId)
    val divisionNameBytes = getDivisionNameBytes(divisionWithId)

    createDivisionForBytes(departments, names, divisionKeyBytes, divisionNameBytes, id.byteArray).map(_ => id.some)
  }

  private[dao] def createDivisionForFreeName(departments: AsyncTable, names: AsyncTable, division: Department[UUID]): Future[Option[UUID]] = {

    val creation = for {
      occupied <- names.exists(createGet(getDivisionNameBytes(division))).asScala
    } yield if (occupied) Future.failed(DepartmentNameIsAlreadyTaken(division.name))
    else prepareAndCreateDivision(departments, names, division)

    creation.flatten
  }


  /**
   *
   * I can encounter following hurdles:
   *  1. The department has been created, but the name was not listed in ''department_name''.
   *     In this case I shall retry deleting the department from ''department'' while keeping the version of the tombstone slightly (+1) higher.
   *     This way the tombstone will never erase records, which come later.
   *  1. The department was not created at all.
   *
   * I think it is not worth it reverting the second operation.
   * Because if it was not applied,
   *
   *
   * I should revert them in reverse order.
   * Though, I think it doesn't matter.
   * Because you either:
   *  1. You see a department, and can create another one with the same name.
   *  1. You don't see a department, but you cannot use its name.
   *     I think the latter is worse.
   *
   *
   * I think the best strategy for retries is to check if such record exists, and then cancel it.
   *
   *
   * Also, I assume the same record won't be queried at the same time (millisecond).
   *
   *
   * When creating the department I must consider occupied names.
   *
   * How the rollback works:
   * Given a key, I shall check whether the object has been inserted successfully, and if yes, revert it.
   * Also, I will propagate error to avoid collisions.
   *
   * @param dep
   * @return
   */
  // Checks if the department has an id. If does, does not create it.
  def create(division: Department[UUID]): Future[Option[UUID]] = usingBothTables { case (departments, names) =>

    division.id.fold(createDivisionForFreeName(departments, names, division))(_ => none[UUID].pure[Future])

  }

  override def update(value: Department[UUID]): Future[Option[Affected]] = ???

  override def readAll(): Future[Seq[Department[UUID]]] = ???

  override def readById(id: UUID): Future[Option[Department[UUID]]] = usingBothTables { case (departments, _) =>

    val division = departments.getScanner(createScanPrefix(id.byteArray)).asScala.headOption

    division.map(_.getRow)

  }

  override def deleteById(id: UUID): Future[Affected] = ???

}