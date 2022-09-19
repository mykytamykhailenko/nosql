package com.aimprosoft.hbase

import com.aimprosoft.hbase.Util.{AsyncTable, depNameTableName, depTableName, empByDepTableName, empTableName}
import org.apache.hadoop.hbase.client.{AsyncAdmin, AsyncConnection, ConnectionFactory}
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.CompletionStageOps

@Singleton
class AsyncConnectionLifecycle @Inject()(applicationLifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext) {

  val connection: Future[AsyncConnection] = ConnectionFactory.createAsyncConnection().asScala

  val departments: Future[AsyncTable] = connection.map(_.getTable(depTableName))

  val names: Future[AsyncTable] = connection.map(_.getTable(depNameTableName))

  val employees: Future[AsyncTable] = connection.map(_.getTable(empTableName))

  val employeesByDep: Future[AsyncTable] = connection.map(_.getTable(empByDepTableName))

  def usingTables[T](calc: ((AsyncTable, AsyncTable, AsyncTable, AsyncTable)) => Future[T]): Future[T] = {

    val res = for {
      dep <- departments
      name <- names
      emp <- employees
      empByDep <- employeesByDep
    } yield calc((dep, name, emp, empByDep))

    res.flatten
  }

  private[dao] def truncateTables(admin: AsyncAdmin) = {

    val tables = Seq(depTableName, depNameTableName, empTableName, empByDepTableName)

    val dropTables = for {
      table <- tables
    } yield for {
      _ <- admin.disableTable(table).asScala
      _ <- admin.deleteTable(table).asScala
    } yield ()

    Future.sequence(dropTables).map(_ => ())
  }

  private[dao] def truncate(): Future[Unit] = {

    val truncation = for {
      admin <- connection.map(_.getAdmin)
    } yield truncateTables(admin)

    truncation.flatten
  }

  applicationLifecycle.addStopHook(() => connection.map(_.close()))

}

