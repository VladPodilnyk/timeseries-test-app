package timeseries.roles

import distage.Lifecycle
import izumi.distage.roles.model.{RoleDescriptor, RoleService}
import izumi.functional.bio.IO2
import izumi.fundamentals.platform.cli.model.raw.RawEntrypointParams
import logstage.LogIO2
import timeseries.utils.CsvDataLoader

import scala.annotation.unused

final class ServiceRole[F[+_, +_]: IO2](
  @unused apiRole: ApiRole[F],
  @unused backendRole: BackendRole[F],
  dataLoader: CsvDataLoader[F],
  log: LogIO2[F],
) extends RoleService[F[Throwable, _]] {
  override def start(roleParameters: RawEntrypointParams, freeArgs: Vector[String]): Lifecycle[F[Throwable, _], Unit] = {
    Lifecycle.liftF {
      for {
        _ <- dataLoader.load("src/main/resources/meterusage.csv")
        _ <- log.info("HTTP and gRPC APIs started!")
      } yield ()
    }
  }
}

object ServiceRole extends RoleDescriptor {
  final val id = "service"
}
