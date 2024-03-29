package com.aimprosoft.hbase

import com.aimprosoft.hbase.Util._
import org.apache.hadoop.hbase.client.{AsyncAdmin, AsyncConnection, ConnectionFactory}
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.CompletionStageOps

@Singleton
class AsyncConnectionLifecycle @Inject()(applicationLifecycle: ApplicationLifecycle, config: Conf)(implicit ec: ExecutionContext) {

  val connection: Future[AsyncConnection] = {

    ConnectionFactory.createAsyncConnection(config.getHadoopConfig).asScala
  }

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

  applicationLifecycle.addStopHook(() => connection.map(_.close()))

}

