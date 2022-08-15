package com.aimprosoft.slick

package object table {

  import databaseConfig.profile.api._

  val employeeTable = TableQuery[EmployeeTable]

  val departmentTable = TableQuery[DepartmentTable]

}
