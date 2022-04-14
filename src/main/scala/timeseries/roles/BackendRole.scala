package timeseries.roles

import distage.Lifecycle
import io.grpc.Server
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.functional.bio.Applicative2
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import logstage.LogIO2

import scala.annotation.unused

final class BackendRole[F[+_, +_]: Applicative2](
  @unused runningServer: Server,
  log: LogIO2[F],
) extends RoleService[F[Throwable, _]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): Lifecycle[F[Throwable, _], Unit] = {
    Lifecycle.liftF(log.info("Grpc backend started!"))
  }
}

object BackendRole extends RoleDescriptor {
  final val id = "backend"
}
