package timeseries.api.grpc

import com.google.protobuf.timestamp.Timestamp
import izumi.functional.bio.{Exit, UnsafeRun2}
import shop.rpc.timeseries_service
import shop.rpc.timeseries_service.{DataWithTimestamp, QueryResponse}
import timeseries.config.LimitsCfg
import timeseries.domain.utils.*
import timeseries.model.UserRequest
import timeseries.repo.TimeSeries

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

final class GrpcService[F[+_, +_]: UnsafeRun2](repo: TimeSeries[F], limitsCfg: LimitsCfg) extends GrpcServerApi[F] {
  override def fetchData(request: timeseries_service.Iterator): Future[QueryResponse] = {
    val dataRequest = UserRequest(request.start.toZonedDateTime, request.end.toZonedDateTime)
    val eff         = repo.fetch(dataRequest, limitsCfg.pageLimit)

    UnsafeRun2[F]
      .unsafeRunAsyncAsFuture(eff)
      .flatMap {
        case Exit.Error(err, _) =>
          Future.failed(new RuntimeException(s"Error: couldn't fetch data due to: ${err.getMessage}"))
        case Exit.Interruption(err, _) =>
          Future.failed(new RuntimeException(s"Interruption: couldn't fetch data due to ${err.getMessage}"))
        case Exit.Termination(err, _, _) =>
          Future.failed(new RuntimeException(s"Termination: couldn't fetch data due to ${err.getMessage}"))
        case Exit.Success(values) =>
          val last           = values.lastOption.map(v => Timestamp(v.timestamp.toEpochSecond))
          val protobufModels = values.map(v => DataWithTimestamp(v.value, Timestamp(v.timestamp.toEpochSecond)))
          Future.successful(QueryResponse(last, protobufModels))
      }(global)
  }
}
