package timeseries.model

import io.circe.{Codec, derivation}

import java.time.ZonedDateTime

final case class ValueWithTimestamp(value: Float, timestamp: ZonedDateTime)
object ValueWithTimestamp {
  implicit val codec: Codec.AsObject[ValueWithTimestamp] = derivation.deriveCodec
}
