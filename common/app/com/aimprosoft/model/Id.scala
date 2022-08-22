package com.aimprosoft.model

/**
 * Represents a record which may or may not contain an [[com.aimprosoft.model.Id Id]].
 */
trait Id[K] {

  val id: Option[K]

}
