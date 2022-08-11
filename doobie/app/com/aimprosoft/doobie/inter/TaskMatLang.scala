package com.aimprosoft.doobie.inter

import com.aimprosoft.common.lang.MatLang
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Future


case class TaskMatLang(sc: Scheduler) extends MatLang[Task] {

  def materialize[M](v: Task[M]): Future[M] = v.runToFuture(sc)

}
