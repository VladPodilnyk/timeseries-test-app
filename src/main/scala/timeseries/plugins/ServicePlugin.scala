package timeseries.plugins

import distage.config.ConfigModuleDef
import distage.plugins.PluginDef
import distage.{ModuleDef, Scene, TagKK}
import doobie.Transactor
import izumi.distage.model.definition.StandardAxis.Repo
import izumi.distage.roles.model.definition.RoleModuleDef
import izumi.fundamentals.platform.integration.PortCheck
import org.http4s.dsl.Http4sDsl
import timeseries.api.grpc.{GrpcServerApi, GrpcService}
import timeseries.api.http.{HttpApi, TimeseriesServiceApi}
import timeseries.domain.DataFetcher
import timeseries.config.{GrpcServerCfg, LimitsCfg, PostgresCfg, PostgresPortCfg}
import timeseries.grpc.{GrpcClient, GrpcServer}
import timeseries.http.HttpServer
import timeseries.repo.TimeSeries
import timeseries.roles.{ApiRole, BackendRole, ServiceRole}
import timeseries.sql.{SQL, TransactorResource}
import timeseries.utils.CsvDataLoader
import zio.IO

import scala.concurrent.duration.*

object ServicePlugin extends PluginDef {
  include(modules.roles[IO])
  include(modules.api[IO])
  include(modules.domain[IO])
  include(modules.utils[IO])
  include(modules.repoDummy[IO])
  include(modules.repoProd[IO])
  include(modules.configs)
  include(modules.prodConfigs)

  object modules {
    def roles[F[+_, +_]: TagKK]: RoleModuleDef = new RoleModuleDef {
      makeRole[ApiRole[F]]
      makeRole[BackendRole[F]]
      makeRole[ServiceRole[F]]
    }

    def api[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[HttpApi[F]].from[TimeseriesServiceApi[F]]
      make[HttpServer.Impl[F]]
      make[HttpServer].fromResource((_: HttpServer.Impl[F]).resource)
      make[Http4sDsl[F[Throwable, _]]]

      make[GrpcServerApi[F]].from[GrpcService[F]]
      make[GrpcClient[F]].from[GrpcClient.GrpcClientImpl[F]]
      make[GrpcServer[F]]
      make[io.grpc.Server].fromResource((_: GrpcServer[F]).resource)
    }

    def domain[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[DataFetcher[F]]
    }

    def utils[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      make[CsvDataLoader[F]]
    }

    def repoDummy[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(Repo.Dummy)

      make[TimeSeries[F]].from[TimeSeries.DummyImpl[F]]
    }

    def repoProd[F[+_, +_]: TagKK]: ModuleDef = new ModuleDef {
      tag(Repo.Prod)
      make[SQL[F]].from[SQL.Impl[F]]

      make[Transactor[F[Throwable, _]]].fromResource[TransactorResource[F[Throwable, _]]]
      make[PortCheck].from(new PortCheck(3.seconds))

      make[TimeSeries[F]].fromResource[TimeSeries.PostgresImpl[F]]
    }

    val configs: ConfigModuleDef = new ConfigModuleDef {
      makeConfig[PostgresCfg]("postgres")
      makeConfig[LimitsCfg]("limits")
      makeConfig[GrpcServerCfg]("grpc")

    }
    val prodConfigs: ConfigModuleDef = new ConfigModuleDef {
      tag(Scene.Provided)
      makeConfig[PostgresPortCfg]("postgres")
    }
  }
}
