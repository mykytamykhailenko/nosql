package com.aimprosoft.hbase

import akka.actor.Scheduler
import akka.pattern.retry
import com.aimprosoft.model.{Affected, Department, Employee}
import io.jvm.uuid._
import org.apache.hadoop.hbase.Cell.Type
import org.apache.hadoop.hbase.CellBuilderType.SHALLOW_COPY
import org.apache.hadoop.hbase.client.{AdvancedScanResultConsumer, Delete, Get, Put, Result, Scan, AsyncTable => HBaseAsyncTable}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.util.Bytes.toBytes
import org.apache.hadoop.hbase.{Cell, CellBuilderFactory, TableName}

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.MapHasAsScala

object Util {

  type ByteArray = Array[Byte]

  val depTableName: TableName = TableName.valueOf("department")

  val depNameTableName: TableName = TableName.valueOf("department_name")

  val empTableName: TableName = TableName.valueOf("employee")

  val empByDepTableName: TableName = TableName.valueOf("employee_by_department_id")

  val departmentBytes: ByteArray = Bytes.toBytes("dp")

  val employeeBytes: ByteArray = Bytes.toBytes("em")

  val nameBytes: ByteArray = Bytes.toBytes("name")

  val surnameBytes: ByteArray = Bytes.toBytes("sn")

  val descBytes: ByteArray = Bytes.toBytes("desc")

  val uuidBytes: ByteArray = Bytes.toBytes("uuid")


  def byteArrayToString(value: ByteArray) = new String(value, StandardCharsets.UTF_8)


  def getEmployee(data: Result): Employee[UUID] = {
    val qualifiers = data.getFamilyMap(employeeBytes).asScala.map { case (qualifier, value) =>
      byteArrayToString(qualifier) -> byteArrayToString(value)
    }

    Employee(
      Some(UUID(data.getRow)),
      UUID(data.getValue(employeeBytes, departmentBytes)),
      qualifiers("name"),
      qualifiers("sn"))
  }

  def getEmployeeOpt(data: Result): Option[Employee[UUID]] = if (data.isEmpty) None else Some(getEmployee(data))

  def getDepartment(data: Result) = {
    val qualifiers = data.getFamilyMap(departmentBytes).asScala.map { case (qualifier, value) =>
      byteArrayToString(qualifier) -> byteArrayToString(value)
    }

    Department(
      Some(UUID(data.getRow)),
      qualifiers("name"),
      qualifiers("desc"))
  }

  def getDepartmentOpt(data: Result): Option[Department[UUID]] = if (data.isEmpty) None else Some(getDepartment(data))

  def createPutForDepartment(division: Department[UUID], currentTime: Long) =

    createPut(division.id.get.byteArray, departmentBytes, currentTime, Map(
      nameBytes -> toBytes(division.name),
      descBytes -> toBytes(division.description)
    ))

  def createPutForName(division: Department[UUID], currentTime: Long) =
    createPut(toBytes(division.name), departmentBytes, currentTime, Map(uuidBytes -> division.id.get.byteArray))

  def createPutForDesc(division: Department[UUID], currentTime: Long) =
    new Put(division.id.get.byteArray).addColumn(departmentBytes, descBytes, currentTime, division.description.getBytes)

  def createDelete(key: ByteArray, currentTime: Long) = new Delete(key).setTimestamp(currentTime)

  def createPut(employee: Employee[UUID], currentTime: Long): Put =
    createPut(employee.id.get.byteArray, employeeBytes, currentTime, Map(
      nameBytes -> toBytes(employee.name),
      surnameBytes -> toBytes(employee.surname),
      departmentBytes -> employee.departmentId.byteArray,
    ))

  def createEmployeeWideKey(v: Employee[UUID]): ByteArray = v match {
    case Employee(Some(em), dp, name, surname) =>
      ByteBuffer.allocate(96)
        .put(dp.byteArray)
        .position(16)
        .put(Bytes.toBytes(surname))
        .position(48)
        .put(Bytes.toBytes(name))
        .position(80)
        .put(em.byteArray)
        .array()
  }

  def buildEmployeeFromWideKey(wideKey: ByteArray): Employee[UUID] = {

    val employeeId = UUID(wideKey.slice(80, 96))

    val departmentId = UUID(wideKey.slice(0, 16))

    val surname = byteArrayToString(wideKey.slice(16, 48)).trim

    val name = byteArrayToString(wideKey.slice(48, 80)).trim

    Employee(Some(employeeId), departmentId, name, surname)
  }

  // This version of Put doesn't store any values.
  // I have a use-case where all my data is stored in the key.
  def createEmptyPut(key: ByteArray, family: ByteArray, timestamp: Long): Put = {

    val cell: Cell =
      CellBuilderFactory
        .create(SHALLOW_COPY)
        .setType(Type.Put)
        .setRow(key)
        .setFamily(family)
        .setTimestamp(timestamp)
        .build()

    new Put(key).add(cell)
  }

  // all operations work with the same column family.
  def createPut(key: ByteArray, family: ByteArray, timestamp: Long, columnAndValue: Map[ByteArray, ByteArray]): Put =
    columnAndValue.foldLeft(new Put(key)) { case (put, (column, value)) =>
      put.addColumn(family, column, timestamp, value)
    }

  def createPrefixScan(prefix: ByteArray) = new Scan().setRowPrefixFilter(prefix)


  def createGet(key: ByteArray): Get = new Get(key)

  type AsyncTable = HBaseAsyncTable[AdvancedScanResultConsumer]


  val noOne: Affected = 0

  val one: Affected = 1

  val aMillisecond = 1
  val nothingUpdated: Future[Option[Affected]] = Future.successful(Some(noOne))

  val nothingDeleted = Future.successful(noOne)


  def retries[T](op: () => Future[T])(implicit ec: ExecutionContext, scheduler: Scheduler): Future[T] =
    retry(op, attempts = 1024, minBackoff = 1.second, maxBackoff = 24.hours, randomFactor = 0.005)


}
