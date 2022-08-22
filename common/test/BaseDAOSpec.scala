import cats.implicits.catsSyntaxOptionId
import com.aimprosoft.model.Employee
import dao.StateDAO
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification
import util.Assign
import util.Identify.intIdentify


class BaseDAOSpec extends Specification with Matchers {

  def createEmployeeState(): StateDAO[Int, Employee[Int]] = StateDAO[Int, Employee[Int]]()

  "base dao" should {

    val employee = Employee(None, 0, "Ugbuemugbem", "Osas")
    val employeeWithId = implicitly[Assign[Int, Employee[Int]]].assign(employee)
    val employeeId = employeeWithId.id.get

    // It is an updated version of the employee.
    val worker = Employee(Some(employeeId), 0, "Ovuvuevuevue", "Enyetuenwuvue")

    "read an instance by id" in {

      val (_, employeeOpt) =
        createEmployeeState()
          .readById(employeeId)
          .run(Map(employeeId -> employeeWithId))
          .value

      employeeOpt === employeeWithId.some
    }

    "read all instances" in {

      val allEmployees = Map(employeeId -> employeeWithId)

      val (_, employeeSeq) =
        createEmployeeState()
          .readAll()
          .run(allEmployees)
          .value

      employeeSeq === allEmployees.values.toSeq
    }


    "create an instance without id" in {

      val (employees, id) =
        createEmployeeState()
          .create(employee)
          .run(Map())
          .value

      employees.head === employeeId -> employeeWithId
      id === employeeId.some
    }

    "not create an instance with id" in {

      val (employees, id) =
        createEmployeeState()
          .create(employeeWithId)
          .run(Map())
          .value

      employees.isEmpty must beTrue
      id === None
    }

    "update an instance with id" in {

      val (employees, affected) =
        createEmployeeState()
          .update(worker)
          .run(Map(employeeId -> employeeWithId))
          .value

      employees.head === employeeId -> worker
      affected === 1.some
    }

    "delete an instance" in {

      val (employees, affected) =
        createEmployeeState()
          .deleteById(employeeId)
          .run(Map(employeeId -> employeeWithId))
          .value

      employees must empty
      affected === 1
    }

    "create, update, read all and delete instances" in {

      val lang = createEmployeeState()

      val affected = for {
        id <- lang.create(employee)
        _ <- lang.update(worker.copy(id = id))
        employees <- lang.readAll()
        _ <- lang.deleteById(id.get)
        theWorker <- lang.readById(id.get)
      } yield (employees, theWorker)

      val (_, (employees, theWorker)) = affected.run(Map()).value

      employees.head === worker
      theWorker must beNone
    }
  }

}
