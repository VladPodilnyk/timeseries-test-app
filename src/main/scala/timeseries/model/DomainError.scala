package timeseries.model

import io.circe.Codec
import io.circe.derivation

final case class DomainError(message: String)
object DomainError {
  implicit val codec: Codec.AsObject[DomainError] = derivation.deriveCodec
}
