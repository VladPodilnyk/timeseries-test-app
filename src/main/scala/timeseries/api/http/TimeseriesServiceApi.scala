package timeseries.api.http

import cats.effect.Blocker
import io.circe.syntax.*
import izumi.functional.bio.{F, IO2}
import izumi.functional.bio.catz.*
import org.http4s.{HttpRoutes, StaticFile}
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import timeseries.domain.DataFetcher
import timeseries.effect.ContextShiftThrowable
import timeseries.model.{DomainError, UserRequest}

import scala.concurrent.ExecutionContext

final class TimeseriesServiceApi[F[+_, +_]: IO2: ContextShiftThrowable](
  dsl: Http4sDsl[F[Throwable, _]],
  dataFetcher: DataFetcher[F],
) extends HttpApi[F] {
  import dsl.*

  private val blocker = Blocker.liftExecutionContext(ExecutionContext.global)

  override def http: HttpRoutes[F[Throwable, *]] = {
    HttpRoutes.of {
      case req @ GET -> Root / "timeseries" =>
        StaticFile
          .fromResource[F[Throwable, _]](s"index.html", blocker, Some(req))
          .getOrElseF(NotFound())

      case rq @ POST -> Root / "fetch" =>
        val eff = for {
          request <- rq.decodeJson[UserRequest]
          _        = println(request)
          result  <- dataFetcher.retrieve(request)
        } yield result.asJson

        Ok(eff.catchAll(err => F.sync(DomainError(err.getMessage).asJson)))
    }
  }
}
