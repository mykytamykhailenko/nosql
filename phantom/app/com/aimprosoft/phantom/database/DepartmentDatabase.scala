package com.aimprosoft.phantom.database

import com.aimprosoft.phantom.table._
import com.outworkers.phantom.dsl._


case class DepartmentDatabase(override val connector: CassandraConnection) extends Database[DepartmentDatabase](connector) {

  object employee extends employee with Connector

  object department extends department with Connector

  object employeeByDepartmentId extends employee_by_department_id with Connector

}
