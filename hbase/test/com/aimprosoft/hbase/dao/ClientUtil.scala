package com.aimprosoft.hbase.dao

import com.aimprosoft.hbase.dao.ClientUtil.RequestOps
import com.aimprosoft.hbase.dao.Util.{departments, employees}
import com.aimprosoft.model.{Department, Employee}
import io.jvm.uuid.UUID
import play.api.http.ContentTypes.JSON
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSRequest}
import play.api.test.Helpers.CONTENT_TYPE

import scala.concurrent.ExecutionContext

case class ClientUtil(client: StandaloneWSClient, host: String, port: Int)(implicit ec: ExecutionContext) {

  def createUrl(resource: String) = s"http://$host:$port/$resource"

  def createUrlWithId(resource: String, id: UUID) = s"http://$host:$port/$resource/$id"

  def createEmployee(departmentId: UUID, name: String, surname: String) =
    client.url(createUrl(employees)).create(Employee(None, departmentId, name, surname))

  def createDepartment(name: String, description: String) =
    client.url(createUrl(departments)).create(Department[UUID](None, name, description))

  def readDepartmentById(id: UUID) =
    client.url(createUrlWithId(departments, id)).get()
      .map(v => Json.parse(v.body))

  def readAllDepartments() =
    client.url(createUrl(departments)).get()
      .map(v => Json.parse(v.body))

  def deleteDepartmentById(id: UUID) =
    client.url(createUrlWithId(departments, id)).delete().map(v => Json.parse(v.body))

  def deleteEmployeeById(id: UUID) =
    client.url(createUrlWithId(employees, id)).delete().map(v => Json.parse(v.body))

  def updateDepartment(dep: Department[UUID]) =
    client.url(createUrl(departments)).update(dep)

  def readEmployeesByDepartmentId(id: UUID) =
    client.url(createUrlWithId(departments, id) + "/" + employees).get()
      .map(v => Json.parse(v.body))

  def readEmployeeById(id: UUID) =
    client.url(createUrlWithId(employees, id)).get()
    .map(v => Json.parse(v.body))

  def updateEmployee(dep: Employee[UUID]) =
    client.url(createUrl(employees)).update(dep)


}

object ClientUtil {


  implicit class RequestOps(request: StandaloneWSRequest)(implicit ec: ExecutionContext) {

    def create[T: Writes](value: T) =
      request
        .withHttpHeaders(CONTENT_TYPE -> JSON)
        .post(Json.toJson(value).toString)
        .map(v => Json.parse(v.body))

    def update[T: Writes](value: T) =
      request
        .withHttpHeaders(CONTENT_TYPE -> JSON)
        .put(Json.toJson(value).toString)
        .map(v => Json.parse(v.body))


  }

}
