package com.aimprosoft.phantom.service

import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{Affected, Department}
import com.aimprosoft.service.{TBasicService, TDepartmentService}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class DepartmentService @Inject()(departments: BasicDAO[Future, UUID, Department[UUID]]) extends TDepartmentService[Future, UUID] {

  def create(dept: Department[UUID]): Future[Option[UUID]] = departments.create(dept)

  def update(dept: Department[UUID]): Future[Option[Affected]] = departments.update(dept)

  def readAll(): Future[Seq[Department[UUID]]] = departments.readAll()

  def readById(id: UUID): Future[Option[Department[UUID]]] = departments.readById(id)

  def deleteById(id: UUID): Future[Affected] = departments.deleteById(id)

}
