package com.aimprosoft.slick.table

import com.aimprosoft.model.Department
import com.aimprosoft.slick.databaseConfig.profile.api._

class DepartmentTable(tag: Tag) extends Table[Department](tag, "departments") with TSlickBaseTable[Department] {

  def id: Rep[Int] = column("department_id", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column("name")

  def description: Rep[String] = column("description")

  def * = (id.?, name, description) <> ((Department.apply _).tupled, Department.unapply)

  def idx = index("department_ix", name)
}

