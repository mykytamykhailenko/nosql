package com.aimprosoft.mongo.dao

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId, none}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Department}
import com.aimprosoft.mongo.connection.Connection
import com.aimprosoft.mongo.converters.Converters._
import com.aimprosoft.mongo.util.Util.recoverDuplicateName
import com.aimprosoft.util.DepartmentExceptions.DepartmentIsNotEmpty
import com.google.inject.Inject
import io.jvm.uuid.UUID
import org.mongodb.scala.model.Filters._

import scala.concurrent.{ExecutionContext, Future}

class DepartmentDAO @Inject()(conn: Connection)(implicit ec: ExecutionContext) extends BasicDAO[Future, UUID, Department[UUID]] {

  import conn._

  override def create(department: Department[UUID]): Future[Option[UUID]] = department.id.fold {

    val creation =
      departments
        .insertOne(departmentToDocument(department.copy(id = UUID.random.some)))
        .map(_.getInsertedId.asBinary.asUuid.some)
        .head()

    recoverDuplicateName(creation, department.name)

  }(_ => none[UUID].pure[Future])

  override def update(department: Department[UUID]): Future[Option[Affected]] = department.id.fold(none[Affected].pure[Future]) { depId =>

    val creation =
      departments
        .replaceOne(equal(id, toBsonBinary(depId)), departmentToDocument(department))
        .map(_.getModifiedCount.toInt.some)
        .head()

    recoverDuplicateName(creation, department.name)
  }

  override def readAll(): Future[Seq[Department[UUID]]] = for {
    divisions <- departments.find().collect().toFuture()
  } yield divisions.flatMap(documentToDepartment)

  override def readById(departmentId: UUID): Future[Option[Department[UUID]]] =
    departments
      .find(equal(id, toBsonBinary(departmentId)))
      .first()
      .toFuture()
      .map(documentToDepartment)

  private[dao] def deleteDepartment(depId: UUID) =
    departments
      .deleteOne(equal(id, toBsonBinary(depId)))
      .map(_.getDeletedCount.toInt)
      .head()

  override def deleteById(depId: UUID): Future[Affected] = {

    val deletion = for {
      worker <- employees.find(equal(departmentId, toBsonBinary(depId))).first().headOption()
    } yield worker.fold(deleteDepartment(depId))(_ => Future.failed(DepartmentIsNotEmpty(depId)))

    deletion.flatten
  }

}