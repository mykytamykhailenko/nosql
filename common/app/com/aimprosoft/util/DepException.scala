package com.aimprosoft.util

import java.util.UUID

object DepException {

  abstract class DepException(message: String) extends Throwable(message)

  case class DepartmentIsNotEmpty(id: UUID) extends DepException(s"Department '$id' is not empty")

  case class DepartmentDoesNotExist(id: UUID) extends DepException(s"Department '$id' does not exist")

  case class DepartmentNameIsAlreadyTaken(name: String) extends DepException(s"'$name' is already taken")

}
