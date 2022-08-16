package com.aimprosoft.slick.table

import com.aimprosoft.model._

import com.aimprosoft.slick.databaseConfig.profile.api._

trait TSlickBaseTable[M <: TIdentity] {
  self: Table[M] =>

  def id: Rep[Id]

}
