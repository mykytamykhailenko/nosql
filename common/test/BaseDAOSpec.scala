import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.common.model.Employee
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import inter.BasicOpStateInterp.{StateActionLang, assign, employeeAssigner}

class BaseDAOSpec extends Specification with Matchers {

  "base dao" should {

    val employee = Employee(None, 0, "Ugbuemugbem", "Osas")
    val employeeWithId = assign(employee, employeeAssigner)
    val employeeId = employeeWithId.id.get

    // It is an updated version of the employee.
    val worker = Employee(Some(employeeId), 0, "Ovuvuevuevue", "Enyetuenwuvue")


    "read an instance by id" in {

      val (_, employeeOpt) = StateActionLang[Employee](employeeAssigner)
        .readById(employeeId)
        .run(Map(employeeId -> employeeWithId))
        .value

      employeeOpt === employeeWithId.some
    }

    "read all instances" in {

      val allEmployees = Map(employeeId -> employeeWithId)

      val (_, employeeSeq) = StateActionLang[Employee](employeeAssigner)
        .readAll()
        .run(allEmployees)
        .value

      employeeSeq === allEmployees.values.toSeq
    }


    "create an instance without id" in {

      val (employees, id) = StateActionLang[Employee](employeeAssigner)
        .create(employee)
        .run(Map())
        .value

      employees.head === employeeId -> employeeWithId
      id === employeeId.some
    }

    "not create an instance with id" in {

      val (employees, id) = StateActionLang[Employee](employeeAssigner)
        .create(employeeWithId)
        .run(Map())
        .value

      employees.isEmpty must beTrue
      id === None
    }

    "update an instance with id" in {

      val (employees, affected) = StateActionLang[Employee](employeeAssigner)
        .update(worker)
        .run(Map(employeeId -> employeeWithId))
        .value

      employees.head === employeeId -> worker
      affected === 1.some
    }

    "delete an instance" in {

      val (employees, affected) = StateActionLang[Employee](employeeAssigner)
        .deleteById(employeeId)
        .run(Map(employeeId -> employeeWithId))
        .value

      employees must empty
      affected === 1
    }

    "create, update, read all and delete instances" in {

      val lang = StateActionLang[Employee](employeeAssigner)

      import lang._

      val affected = for {
        id <- create(employee)
        _ <- update(worker.copy(id = id))
        employees <- readAll()
        _ <- deleteById(id.get)
        theWorker <- readById(id.get)
      } yield (employees, theWorker)

      val (_, (employees, theWorker)) = affected.run(Map()).value

      employees.head === worker
      theWorker must beNone
    }
  }

}
