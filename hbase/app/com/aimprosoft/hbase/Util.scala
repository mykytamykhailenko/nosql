package com.aimprosoft.hbase

import akka.actor.Scheduler
import akka.dispatch.Dispatcher
import akka.pattern.retry
import com.aimprosoft.model.{Affected, Department, Employee}
import io.jvm.uuid._
import org.apache.hadoop.hbase.Cell.Type
import org.apache.hadoop.hbase.CellBuilderType.SHALLOW_COPY
import org.apache.hadoop.hbase.client.{AdvancedScanResultConsumer, CheckAndMutate, CheckAndMutateResult, Delete, Get, Put, Result, Scan, AsyncTable => HBaseAsyncTable}
import org.apache.hadoop.hbase.filter.KeyOnlyFilter
import org.apache.hadoop.hbase.io.TimeRange
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.util.Bytes.toBytes
import org.apache.hadoop.hbase.{Cell, CellBuilderFactory, TableName}

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.MapHasAsScala
import scala.jdk.FutureConverters.CompletionStageOps

object Util {

  type ByteArray = Array[Byte]

  type ToByteArray[M] = M => ByteArray

  val byteBufferSize = 98

  /*
  'department_name' -> 128
  'name' -> 32
  'surname' -> 32
  'uuid' - 16
   */

  // I will need to add validation

  // 'department_name' -> 'dp:id''department_id'
  def getDivisionNameBytes(v: Department[UUID]): ByteArray = Bytes.toBytes(v.name)

  // Updates will become more simple, because I do not need to update the row key.
  // Again, 'name' must not be a part of the primary key.
  // 'department_id' -> 'dp:name''department_name'
  //                 -> 'dp:desc''description'
  def getDivisionIdBytes(v: Department[UUID]): ByteArray = v.id.get.byteArray

  def byteArrayToString(value: ByteArray) = new String(value, StandardCharsets.UTF_8)

  val nameBytes: Array[Byte] = Bytes.toBytes("name")
  val descBytes: Array[Byte] = Bytes.toBytes("desc")
  val uuidBytes: Array[Byte] = Bytes.toBytes("uuid")


  def resultToDepartment(data: Result) = {
    val qualifiers = data.getFamilyMap(dpBytes).asScala.map { case (qualifier, value) =>
      byteArrayToString(qualifier) -> byteArrayToString(value)
    }

    Department(
      Some(UUID(data.getRow)),
      qualifiers("name"),
      qualifiers("desc"))
  }


  def createPutForDepartment(division: Department[UUID], currentTime: Long) =

    createPut(division.id.get.byteArray, dpBytes, currentTime, Map(
      nameBytes -> toBytes(division.name),
      descBytes -> toBytes(division.description)
    ))

  def createPutForName(division: Department[UUID], currentTime: Long) =
    createPut(toBytes(division.name), dpBytes, currentTime, Map(uuidBytes -> division.id.get.byteArray))

  def createPutForDesc(division: Department[UUID], currentTime: Long) =
    new Put(division.id.get.byteArray).addColumn(dpBytes, descBytes, currentTime, division.description.getBytes)


  // Previously, I included name and surname in the primary key.
  // But in fact, they were redundant.
  // If I want to query employees by 'surname' and 'name' I would need to create another table.
  // Materialized views would not work here because 'surname' and 'name' are not included in the materialized views.
  //
  //
  // 'employee_id' -> 'em:dp''department_id'
  //               -> 'em:name''name'
  //               -> 'em:sn''surname'
  def buildEmpKeyAsByteArray(v: Employee[UUID]): ByteArray = v match {
    case Employee(Some(em), _, name, surname) =>
      ByteBuffer.allocate(80)
        .put(em.byteArray, 0, 16)
        .put(Bytes.toBytes(surname), 16, 32)
        .put(Bytes.toBytes(name), 48, 32)
        .array()
  }


  // 'department_id''surname''name''employee_id' -> 'em:'''
  def buildEmpByDepKeyAsByteArray(v: Employee[UUID]): ByteArray = v match {
    case Employee(Some(em), dp, name, surname) =>
      ByteBuffer.allocate(96)
        .put(dp.byteArray, 0, 16)
        .put(Bytes.toBytes(surname), 16, 32)
        .put(Bytes.toBytes(name), 48, 32)
        .put(em.byteArray, 80, 16)
        .array()
  }

  val depTableName: TableName = TableName.valueOf("department")

  val depNameTableName: TableName = TableName.valueOf("department_name")

  val empTableName: TableName = TableName.valueOf("employee")

  val empByDepTableName: TableName = TableName.valueOf("employee_by_department_id")


  val dpBytes: ByteArray = Bytes.toBytes("dp")

  val emBytes: ByteArray = Bytes.toBytes("em")

  val uuidColumnBytes: ByteArray = Bytes.toBytes("uuid")

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


  def createGet(key: ByteArray): Get = new Get(key)

  def createScanPrefix(key: ByteArray): Scan = new Scan().setRowPrefixFilter(key).setOneRowLimit()

  type AsyncTable = HBaseAsyncTable[AdvancedScanResultConsumer]

  val oneMillisecond = 1

  val unaffected: Affected = 0

  val one: Affected = 1

  def retries[T](op: () => Future[T])
                (implicit ec: ExecutionContext, scheduler: Scheduler): Future[T] =
    retry(op,
      attempts = 1024,
      minBackoff = 1.second,
      maxBackoff = 24.hours,
      randomFactor = 0.005)

  /**
   * Attempts to roll back an event.
   *
   * You should run this method in the background to avoid blocking the client.
   *
   * This method will delete the event only if:
   *  1. The event with this key exists
   *  1. The timestamp of the event and the provided timestamp match
   *
   * This method will never delete an older versions of the event or its never versions (even if
   * the tombstone comes later, its timestamp will be too old to take effect).
   *
   * In case Play crashes, the database will be left in an inconsistent state.
   *
   * @param table     Affected table.
   * @param key       Affected key.
   * @param timestamp The key's timestamp.
   * @param ec        Execution context.
   * @param scheduler Akka's scheduler.
   * @return A failed future containing the initial cause of the failure.
   */
  // I can create a mock AsyncTable, which fails first time, and then calls the real one.
  def rollbackByKey(table: AsyncTable, key: ByteArray, timestamp: Long)
                   (implicit ec: ExecutionContext, scheduler: Scheduler): Future[CheckAndMutateResult] = {

    val drop = new Delete(key).setTimestamp(timestamp + oneMillisecond)

    val checkTimeAndDrop =
      CheckAndMutate
        .newBuilder(key)
        .ifMatches(new KeyOnlyFilter())
        .timeRange(TimeRange.at(timestamp))
        .build(drop)

    // It is a great use case for circuit breaker.
    retries(() => table.checkAndMutate(checkTimeAndDrop).asScala)
  }

}
