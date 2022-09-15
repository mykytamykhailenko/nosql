package com.aimprosoft.hbase.service

import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxOptionId}
import com.aimprosoft.dao.BasicDAO
import com.aimprosoft.model.{CompleteEmployee, Department, Employee}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mock.Mockito.mock
import org.specs2.mutable.Specification
import cats.syntax.option
import com.aimprosoft.hbase.dao.TEmployeeDAO

import java.util.UUID
import scala.concurrent.Future

class EmployeeServiceSpec(implicit ee: ExecutionEnv) extends Specification with FutureMatchers {

  "employee service" should {

    "get an employee with department if both present" in {

      val employees = mock[TEmployeeDAO]

      val departments = mock[BasicDAO[Future, UUID, Department[UUID]]]

      val service = EmployeeService(employees, departments)

      val employeeId = UUID.randomUUID()
      val departmentId = UUID.randomUUID()

      val employee = Employee(employeeId.some, departmentId, "John", "Smith")
      val department = Department(departmentId.some, "QA", "")

      when(employees.readById(employeeId)).thenReturn(employee.some.pure[Future])
      when(departments.readById(departmentId)).thenReturn(department.some.pure[Future])

      service.getCompleteEmployeeById(employeeId) must beSome(CompleteEmployee(employeeId.some, department, "John", "Smith")).await
    }

    "get nothing if the department is absent" in {

      val employees = mock[TEmployeeDAO]

      val departments = mock[BasicDAO[Future, UUID, Department[UUID]]]

      val service = EmployeeService(employees, departments)

      val employeeId = UUID.randomUUID()

      val employee = Employee(employeeId.some, UUID.randomUUID(), "John", "Smith")

      when(employees.readById(employeeId)).thenReturn(employee.some.pure[Future])
      when(departments.readById(any())).thenReturn(option.none[Department[UUID]].pure[Future])

      service.getCompleteEmployeeById(employeeId) must beNone.await
    }

    "get nothing if the employee is absent" in {

      val employees = mock[TEmployeeDAO]

      val departments = mock[BasicDAO[Future, UUID, Department[UUID]]]

      val service = EmployeeService(employees, departments)

      when(employees.readById(any())).thenReturn(option.none[Employee[UUID]].pure[Future])

      service.getCompleteEmployeeById(UUID.randomUUID()) must beNone.await
    }

  }

}
