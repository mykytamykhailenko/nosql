package com.aimprosoft.model

import com.aimprosoft.Util.StringOps

/**
 * Unfortunately, it is hardly possible to generalize Doobie queries, because it is a mere JDBC layer, not an ORM.
 * Therefore, those fields and methods are required for writing queries (if you have a closer look, you may notice that
 * Slick has all those fields already defined in Table).
 *
 * Besides, because you have to build queries yourself, so they are tightly bound to the database SQL syntax.
 *
 * @tparam T Model with identity
 */
trait GetFields[T <: TIdentity] {

  val table: String

  val id: String

  val fieldsWithoutId: Seq[String]

  def getId(v: T): String

  def getFieldMapWithoutId(v: T): Map[String, String]

}

object GetFields {

  def convertOptIdToStr(id: Option[Id]): String = id.fold("NULL")(_.toString)

  implicit val departmentGetFields: GetFields[Department] = new GetFields[Department] {

    val table = "departments"

    val id = "department_id"

    val fieldsWithoutId: Seq[String] = Seq("name", "description")

    def getId(v: Department): String = convertOptIdToStr(v.id)

    def getFieldMapWithoutId(v: Department): Map[String, String] = Map(
      "name" -> v.name.quote,
      "description" -> v.description.quote)
  }

  implicit val employeeGetFields: GetFields[Employee] = new GetFields[Employee] {

    val table = "employees"

    val id = "employee_id"

    val fieldsWithoutId: Seq[String] = Seq("department_id", "name", "surname")

    def getId(v: Employee): String = convertOptIdToStr(v.id)

    def getFieldMapWithoutId(v: Employee): Map[String, String] = Map(
      "department_id" -> v.departmentId.toString,
      "name" -> v.name.quote,
      "surname" -> v.surname.quote)
  }

}