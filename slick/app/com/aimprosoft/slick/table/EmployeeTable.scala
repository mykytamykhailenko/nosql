package com.aimprosoft.slick.table

import com.aimprosoft.model.Employee
import com.aimprosoft.slick.databaseConfig.profile.api._

class EmployeeTable(tag: Tag) extends Table[Employee](tag, "employees") with TSlickBaseTable[Employee] {

  override def id: Rep[Int] = column("employee_id", O.PrimaryKey, O.AutoInc)

  def departmentId: Rep[Int] = column("department_id")

  def name: Rep[String] = column("name")

  def surname: Rep[String] = column("surname")

  def * = (id.?, departmentId, name, surname) <> ((Employee.apply _).tupled, Employee.unapply)

  def department = foreignKey("employee_in_department", departmentId, TableQuery[DepartmentTable])(_.id, onDelete = ForeignKeyAction.Restrict)

  def idx = index("employee_ix", (name, surname))

}