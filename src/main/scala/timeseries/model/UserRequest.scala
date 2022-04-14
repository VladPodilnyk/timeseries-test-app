package timeseries.model

import io.circe.{Codec, derivation}

import java.time.ZonedDateTime

final case class UserRequest(start: ZonedDateTime, end: ZonedDateTime)
object UserRequest {
  implicit val codec: Codec.AsObject[UserRequest] = derivation.deriveCodec
}
