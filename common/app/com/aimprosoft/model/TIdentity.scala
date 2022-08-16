package com.aimprosoft.model

/**
 * Represents a record which may or may not contain an [[com.aimprosoft.model.Id Id]].
 */
trait TIdentity {

  val id: Option[Id]

}
