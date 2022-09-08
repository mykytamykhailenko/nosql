import org.testcontainers.containers.CassandraContainer
import org.testcontainers.utility.DockerImageName
import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse

import java.time.Duration
import scala.concurrent.{ExecutionContext, Future}

object Util {


  val once = 1
  val cassandraPort = 9042

  val playPort = 9000

  val cassandraWaitDuration: Duration = Duration.ofMinutes(3)

  class ScalaCassandraContainer(image: DockerImageName) extends CassandraContainer[ScalaCassandraContainer](image)

  implicit class WSResponseOps(response: Future[WSResponse])(implicit ec: ExecutionContext) {

    def asJson[T: Reads]: Future[T] = response.map(_.json.as[T])

    def asJsonOpt[T: Reads]: Future[Option[T]] = response.map(_.json.asOpt[T])

  }

}
