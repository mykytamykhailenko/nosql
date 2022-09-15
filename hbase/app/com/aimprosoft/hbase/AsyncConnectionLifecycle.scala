package com.aimprosoft.hbase

import org.apache.hadoop.hbase.client.{AsyncConnection, ConnectionFactory}
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters.CompletionStageOps

@Singleton
class AsyncConnectionLifecycle @Inject()(applicationLifecycle: ApplicationLifecycle)(implicit ec: ExecutionContext) {

  val connection: Future[AsyncConnection] = ConnectionFactory.createAsyncConnection().asScala

  applicationLifecycle.addStopHook(() => connection.map(_.close()))

}

