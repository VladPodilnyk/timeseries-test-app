package timeseries

import distage.StandardAxis.Repo
import distage.plugins.PluginConfig
import distage.{Activation, Module, ModuleDef}
import izumi.distage.model.definition.StandardAxis.Scene
import izumi.distage.roles.RoleAppMain
import izumi.fundamentals.platform.cli.model.raw.RawRoleParams
import timeseries.roles.ServiceRole
import zio.IO

object Stub extends MainBase(Activation(Repo -> Repo.Dummy), Vector(RawRoleParams(ServiceRole.id)))
object ManagedDocker extends MainBase(Activation(Repo -> Repo.Prod, Scene -> Scene.Managed), Vector(RawRoleParams(ServiceRole.id)))
object ProvidedDocker extends MainBase(Activation(Repo -> Repo.Prod, Scene -> Scene.Provided), Vector(RawRoleParams(ServiceRole.id)))


object GenericLauncher extends MainBase(Activation(Repo -> Repo.Prod, Scene -> Scene.Provided), Vector.empty)

sealed abstract class MainBase(
  activation: Activation,
  requiredRoles: Vector[RawRoleParams],
) extends RoleAppMain.LauncherBIO2[IO] {

  override def requiredRoles(argv: RoleAppMain.ArgV): Vector[RawRoleParams] = {
    requiredRoles
  }

  override def pluginConfig: PluginConfig = {
    PluginConfig.cached(pluginsPackage = "timeseries.plugins")
  }

  protected override def roleAppBootOverrides(argv: RoleAppMain.ArgV): Module = super.roleAppBootOverrides(argv) ++ new ModuleDef {
    make[Activation].named("default").fromValue(defaultActivation ++ activation)
  }

  private[this] def defaultActivation = Activation(Scene -> Scene.Provided)
}
