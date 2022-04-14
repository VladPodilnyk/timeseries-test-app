package timeseries.api.http

import org.http4s.HttpRoutes

trait HttpApi[F[_, _]] {
  def http: HttpRoutes[F[Throwable, _]]
}
