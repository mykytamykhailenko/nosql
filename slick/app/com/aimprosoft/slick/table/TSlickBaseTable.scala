package com.aimprosoft.slick.table

import com.aimprosoft.model._

import com.aimprosoft.slick.databaseConfig.profile.api._

trait TSlickBaseTable[M <: Id[Int]] {
  self: Table[M] =>

  def id: Rep[Int]

}
