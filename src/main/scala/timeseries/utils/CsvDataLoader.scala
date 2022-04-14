package timeseries.utils

import cats.effect.Blocker
import fs2.*
import izumi.functional.bio.Async2
import izumi.functional.bio.catz.*
import izumi.fundamentals.platform.time.IzTime
import timeseries.effect.ContextShiftThrowable
import timeseries.repo.TimeSeries

import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZonedDateTime}
import scala.concurrent.ExecutionContext
import scala.util.Try

final class CsvDataLoader[F[+_, +_]: Async2: ContextShiftThrowable](repo: TimeSeries[F]) {
  private val blockingExecutionContext = Blocker.liftExecutionContext(ExecutionContext.global)
  private val formatter                = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def load(path: String): F[Throwable, Unit] = {
    io.file
      .readAll[F[Throwable, _]](Paths.get(path), blockingExecutionContext, 4096)
      .through(csvParser)
      .dropWhile(_.isEmpty)
      .map(parseRow)
      .unNoneTerminate
      .evalMap { case (time, value) => repo.submit(time, value) }
      .compile
      .drain
  }

  private[this] def csvParser: Pipe[F[Throwable, _], Byte, List[String]] = {
    _.through(text.utf8Decode[F[Throwable, _]])
      .through(text.lines[F[Throwable, _]])
      .drop(1)
      .map(_.split(',').toList)
  }

  private[this] def parseRow(row: List[String]): Option[(ZonedDateTime, Double)] = {
    row match {
      case timestamp :: value :: Nil =>
        Try {
          val timeMark    = LocalDateTime.parse(timestamp, formatter).atZone(IzTime.TZ_UTC)
          val doubleValue = value.toDouble
          timeMark -> doubleValue
        }.toOption
      case _ => None
    }
  }
}
