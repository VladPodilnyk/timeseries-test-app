package timeseries.api.grpc

import shop.rpc.timeseries_service
import shop.rpc.timeseries_service.{QueryResponse, TimeSeriesGrpc}

import scala.concurrent.Future

trait GrpcServerApi[F[+_, +_]] extends TimeSeriesGrpc.TimeSeries {
  def fetchData(request: timeseries_service.Iterator): Future[QueryResponse]
}
