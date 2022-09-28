package com.aimprosoft.mongo.util

import com.aimprosoft.util.DepartmentExceptions.DepartmentNameIsAlreadyTaken
import com.mongodb.MongoWriteException

import scala.concurrent.{ExecutionContext, Future}

object Util {

  def recoverDuplicateName[T](creation: Future[T], name: String)(implicit ec: ExecutionContext): Future[T] =
    creation
      .recoverWith[T] { case ex: MongoWriteException if ex.getCode == 11000 =>
        Future.failed[T](DepartmentNameIsAlreadyTaken(name))
      }

}
