package com.aimprosoft.mongo.converters

import com.aimprosoft.model.{Department, Employee}
import org.mongodb.scala.bson.{BsonBinary, BsonString, Document}

import java.util.UUID

object Converters {

  val id = "_id"

  val departmentId = "department_id"

  val name = "name"

  val surname = "surname"

  val description = "description"

  def documentToEmployee(document: Document): Option[Employee[UUID]] =
    for {
      id <- document.get[BsonBinary](id)
      departmentId <- document.get[BsonBinary](departmentId)
      name <- document.get[BsonString](name)
      surname <- document.get[BsonString](surname)
    } yield Employee(Some(id.asUuid()), departmentId.asUuid(), name.getValue, surname.getValue)

  def documentToDepartment(document: Document): Option[Department[UUID]] =
    for {
      id <- document.get[BsonBinary](id)
      name <- document.get[BsonString](name)
      description <- document.get[BsonString](description)
    } yield Department(Some(id.asUuid()), name.getValue, description.getValue)

  def toBsonBinary(id: UUID) = new BsonBinary(id)

  def employeeToDocument(employee: Employee[UUID]): Document = employee match {
    case Employee(Some(id), departmentId, name, surname) =>
      Document(
        "_id" -> toBsonBinary(id),
        "department_id" -> toBsonBinary(departmentId),
        "name" -> name,
        "surname" -> surname)
  }

  def departmentToDocument(department: Department[UUID]): Document = department match {
    case Department(Some(id), name, description) =>
      Document(
        "_id" -> toBsonBinary(id),
        "name" -> name,
        "description" -> description)
  }

}
