package timeseries.grpc

import io.grpc.ManagedChannelBuilder
import izumi.functional.bio.{Async2, F, IO2}
import shop.rpc.timeseries_service
import shop.rpc.timeseries_service.{DataWithTimestamp, QueryResponse, TimeSeriesGrpc}
import timeseries.config.GrpcServerCfg

import scala.util.Random

trait GrpcClient[F[+_, +_]] {
  def fetchData(iterator: timeseries_service.Iterator): F[Throwable, QueryResponse]
}

object GrpcClient {
  final class DummyImpl[F[+_, +_]: IO2] extends GrpcClient[F] {
    override def fetchData(iterator: timeseries_service.Iterator): F[Throwable, QueryResponse] = {
      F.syncThrowable {
        val rndInd = new Random().nextInt(30).toFloat
        QueryResponse(Some(iterator.start), List(DataWithTimestamp(rndInd, iterator.start)))
      }
    }
  }

  final class GrpcClientImpl[F[+_, +_]: Async2](grpcServerCfg: GrpcServerCfg) extends GrpcClient[F] {
    import grpcServerCfg.*
    override def fetchData(iterator: timeseries_service.Iterator): F[Throwable, QueryResponse] = {
      val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build
      val stub    = TimeSeriesGrpc.stub(channel)
      F.fromFuture(stub.fetchData(iterator))
    }
  }
}
