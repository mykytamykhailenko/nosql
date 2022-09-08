package com.aimprosoft.phantom.database

import com.aimprosoft.phantom.connection.Connection
import com.aimprosoft.phantom.table._
import com.outworkers.phantom.dsl._

import javax.inject.Inject

class DepartmentDatabase @Inject() (conn: Connection) extends Database[DepartmentDatabase](conn.getCassandraConnection()) {

  object employees extends employee with Connector

  object employeeByDepartmentId extends employee_by_department_id with Connector

  object departments extends department with Connector

  object departmentNames extends department_name with Connector

}
