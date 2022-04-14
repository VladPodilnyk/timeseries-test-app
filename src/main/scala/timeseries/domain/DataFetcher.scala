package timeseries.domain

import com.google.protobuf.timestamp.Timestamp
import izumi.functional.bio.{Error2, F}
import shop.rpc.timeseries_service
import timeseries.domain.utils.*
import timeseries.grpc.GrpcClient
import timeseries.model.{PagedData, UserRequest, ValueWithTimestamp}

final class DataFetcher[F[+_, +_]: Error2](client: GrpcClient[F]) {
  def retrieve(query: UserRequest): F[Throwable, PagedData] = {
    for {
      iterator      <- validateRequest(query)
      queryResponse <- client.fetchData(iterator)
      results        = queryResponse.data.map(v => ValueWithTimestamp(v.value, v.receivedTime.toZonedDateTime)).toList
    } yield PagedData(queryResponse.lastProcessed.map(_.toZonedDateTime), results)
  }

  private[this] def validateRequest(request: UserRequest): F[Throwable, timeseries_service.Iterator] = {
    val start = request.start.toEpochSecond
    val end   = request.end.toEpochSecond
    for {
      _ <- F.when(start > end) {
        F.fail(DataFetcher.invalidRequestError(request))
      }
      iterator = timeseries_service.Iterator(Timestamp(start), Timestamp(end))
    } yield iterator
  }
}

object DataFetcher {
  def invalidRequestError(rq: UserRequest): RuntimeException = {
    new RuntimeException(s"Invalid iterator error. Start time greater than end time: start=${rq.start}, end=${rq.end}")
  }
}
