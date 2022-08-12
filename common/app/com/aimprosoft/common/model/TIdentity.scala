package com.aimprosoft.common.model

/**
 * Represents a record which may or may not contain an [[com.aimprosoft.common.model.Id Id]].
 */
trait TIdentity {

  val id: Option[Id]

}
