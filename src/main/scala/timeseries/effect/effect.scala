package timeseries

import cats.effect.ContextShift

package object effect {
  final type ContextShiftThrowable[F[_, _]] = ContextShift[F[Throwable, _]]
  object ContextShiftThrowable {
    def apply[F[_, _] : ContextShiftThrowable]: ContextShiftThrowable[F] = implicitly
  }
}
