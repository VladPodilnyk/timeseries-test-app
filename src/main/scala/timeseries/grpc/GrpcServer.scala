package timeseries.grpc

import io.grpc.{Server, ServerBuilder}
import izumi.distage.model.definition.Lifecycle
import izumi.functional.bio.{F, IO2}
import shop.rpc.timeseries_service.TimeSeriesGrpc
import timeseries.api.grpc.GrpcServerApi
import timeseries.config.GrpcServerCfg

import scala.concurrent.ExecutionContext

final class GrpcServer[F[+_, +_]: IO2](
  grpcServerApi: GrpcServerApi[F],
  config: GrpcServerCfg,
) {
  def resource: Lifecycle[F[Throwable, _], Server] = {
    Lifecycle.make[F[Throwable, _], Server] {
      F.syncThrowable {
        ServerBuilder
          .forPort(config.port)
          .addService(TimeSeriesGrpc.bindService(grpcServerApi, ExecutionContext.global))
          .build()
          .start()
      }
    }(server => F.syncThrowable(server.shutdown()).void)
  }
}
