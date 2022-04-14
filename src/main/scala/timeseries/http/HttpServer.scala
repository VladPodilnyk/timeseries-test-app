package timeseries.http

import cats.effect.{ConcurrentEffect, Timer}
import distage.Id
import izumi.distage.model.definition.Lifecycle
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import org.http4s.syntax.kleisli.*
import timeseries.api.http.HttpApi

import scala.concurrent.ExecutionContext

final case class HttpServer(
  server: Server
)

object HttpServer {

  final class Impl[F[+_, +_]](
    httpApis: HttpApi[F],
    cpuPool: ExecutionContext @Id("zio.cpu"),
  )(implicit
    concurrentEffect: ConcurrentEffect[F[Throwable, _]],
    timer: Timer[F[Throwable, _]],
  ) {
    def resource: Lifecycle[F[Throwable, _], HttpServer] = {
      Lifecycle.fromCats {
        BlazeServerBuilder[F[Throwable, _]](cpuPool)
          .withHttpApp(httpApis.http.orNotFound)
          .bindLocal(8080)
          .resource
          .map(HttpServer(_))
      }
    }
  }

}
