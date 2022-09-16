package com.aimprosoft.hbase

import com.aimprosoft.hbase.Util._
import org.apache.hadoop.hbase.TableName
import org.apache.hadoop.hbase.client.{ColumnFamilyDescriptorBuilder, ConnectionFactory, TableDescriptor, TableDescriptorBuilder}

import javax.inject.{Inject, Singleton}

@Singleton
class Initializer @Inject()() {

  {
    // A synchronous client for ease of use.
    val connection = ConnectionFactory.createConnection()

    val admin = connection.getAdmin

    import admin.{createTable, tableExists}

    def createTableDescriptor(name: TableName, columnFamily: ByteArray): TableDescriptor =
      TableDescriptorBuilder
        .newBuilder(name)
        .setColumnFamily(ColumnFamilyDescriptorBuilder.newBuilder(columnFamily).build())
        .build()

    // I have decided to stick to the previous approach.
    // 'department_id':'name' -> 'description'

    if (!tableExists(depTableName))
      createTable(createTableDescriptor(depTableName, dpBytes))
    // 'name':'department_id'
    if (!tableExists(depNameTableName))
      createTable(createTableDescriptor(depNameTableName, dpBytes))

    // I can revert failed transaction by keeping time correctly.
    if (!tableExists(empTableName))
      createTable(createTableDescriptor(empTableName, emBytes))

    // HBase does not support clustering keys, so I need to stick to a special pattern.
    // Because HBase stores everything together,
    // 'department_id':'surname':'name':'id'
    // I could move 'id' to column qualifiers, but it may degrade performance.
    // Also, I have com
    if (!tableExists(empByDepTableName))
      createTable(createTableDescriptor(empByDepTableName, emBytes))

    connection.close()
  }

}
