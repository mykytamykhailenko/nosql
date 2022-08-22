package com.aimprosoft.phantom.table

import com.aimprosoft.model.Department
import com.outworkers.phantom.dsl._

abstract class department extends Table[department, Department[UUID]] {

  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn with ClusteringOrder

  object description extends StringColumn with ClusteringOrder

}
