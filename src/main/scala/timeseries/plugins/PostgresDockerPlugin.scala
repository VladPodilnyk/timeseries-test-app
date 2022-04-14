package timeseries.plugins

import distage.Scene
import izumi.distage.docker.Docker.DockerPort
import izumi.distage.docker.bundled.PostgresDocker
import izumi.distage.docker.modules.DockerSupportModule
import izumi.distage.plugins.PluginDef
import timeseries.config.PostgresPortCfg
import zio.Task

object PostgresDockerPlugin extends PluginDef {
  tag(Scene.Managed)

  include(DockerSupportModule[Task])
  make[PostgresDocker.Container]
    .fromResource(PostgresDocker.make[Task])

  make[PostgresPortCfg].from {
    docker: PostgresDocker.Container =>
      val knownAddress = docker.availablePorts.first(DockerPort.TCP(5432))
      PostgresPortCfg(knownAddress.hostString, knownAddress.port)
  }
}
