package com.aimprosoft.model

import com.aimprosoft.Util.StringOps

trait GetFields[T <: TIdentity] {

  val table: String

  val id: String

  val fieldsWithoutId: Seq[String]

  def getId(v: T): String

  def getFieldMap(v: T): Map[String, String]

}

object GetFields {

  def convertOptIdToStr(id: Option[Id]): String = id.fold("NULL")(_.toString)

  implicit val departmentGetFields: GetFields[Department] = new GetFields[Department] {

    val table = "departments"

    val id = "department_id"

    val fieldsWithoutId: Seq[String] = Seq("name", "description")

    def getId(v: Department): String = convertOptIdToStr(v.id)

    def getFieldMap(v: Department): Map[String, String] = Map(
      "department_id" -> getId(v),
      "name" -> v.name.quote,
      "description" -> v.description.quote)
  }

  implicit val employeeGetFields: GetFields[Employee] = new GetFields[Employee] {

    val table = "employees"

    val id = "employee_id"

    val fieldsWithoutId: Seq[String] = Seq("department_id", "name", "surname")

    def getId(v: Employee): String = convertOptIdToStr(v.id)

    def getFieldMap(v: Employee): Map[String, String] = Map(
      "employee_id" -> getId(v),
      "department_id" -> v.departmentId.toString,
      "name" -> v.name.quote,
      "surname" -> v.surname.quote)
  }

}