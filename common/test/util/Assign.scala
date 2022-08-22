package util

import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.model.{Department, Employee, Id}

trait Assign[K, M <: Id[K]] {

  def assign(model: M): M

}

object Assign {

  implicit def employeeAssigner[K](implicit ident: Identify[K]): Assign[K, Employee[K]] = model => model.copy(id = ident.identify(model).some)

  implicit def departmentAssigner[K](implicit ident: Identify[K]): Assign[K, Department[K]] = model => model.copy(id = ident.identify(model).some)


}