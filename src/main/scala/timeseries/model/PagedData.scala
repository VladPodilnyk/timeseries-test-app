package timeseries.model

import io.circe.{Codec, derivation}

import java.time.ZonedDateTime

final case class PagedData(lastTimestamp: Option[ZonedDateTime], values: List[ValueWithTimestamp])
object PagedData {
  implicit val codec: Codec.AsObject[PagedData] = derivation.deriveCodec
}
