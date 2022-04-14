package timeseries.domain

import com.google.protobuf.timestamp.Timestamp

import java.time.{Instant, ZoneId, ZonedDateTime}

object utils {
  implicit final class TimestampOps(timestamp: Timestamp) {
    def toZonedDateTime: ZonedDateTime = {
      val instant = Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong)
      instant.atZone(ZoneId.of("UTC"))
    }
  }

  implicit final class ZonedDateTimeOps(timestamp: ZonedDateTime) {
    def toTimestamp: Timestamp = Timestamp(timestamp.toEpochSecond)
  }
}
