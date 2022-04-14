package timeseries.roles

import distage.Lifecycle
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.functional.bio.Applicative2
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import logstage.LogIO2
import timeseries.http.HttpServer

import scala.annotation.unused

final class ApiRole[F[+_, +_]: Applicative2](
  @unused runningServer: HttpServer,
  log: LogIO2[F],
) extends RoleService[F[Throwable, _]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): Lifecycle[F[Throwable, _], Unit] = {
    Lifecycle.liftF(log.info("Timeseries API started!"))
  }
}

object ApiRole extends RoleDescriptor {
  final val id = "api"
}
