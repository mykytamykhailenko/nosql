package com.aimprosoft.slick.table

import com.aimprosoft.model.Department
import com.aimprosoft.slick.databaseConfig.profile.api._

class DepartmentTable(tag: Tag) extends Table[Department[Int]](tag, "departments") with TSlickBaseTable[Department[Int]] {

  def id: Rep[Int] = column("department_id", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column("name")

  def description: Rep[String] = column("description")

  def * = (id.?, name, description) <> ((Department.apply[Int] _).tupled, Department.unapply[Int])

  def idx = index("department_ix", name)
}

