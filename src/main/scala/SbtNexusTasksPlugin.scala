package sbtnexustasks

import sbt._
import sbt.Keys._

import sbtnexustasks.impl._

object SbtNexusTasksPlugin extends AutoPlugin {

  object autoImport {

    /**
      * Nexus connection settings.
      *
      * Uses the following system properties by default:
      * - `nexustasks.url`
      * - `nexustasks.username`
      * - `nexustasks.password`
      *
      * If any of these properties is missing, defaults to `None`
      */
    val nexusSettings = SettingKey[Option[NexusSettings]](
      "nexusSettings",
      "Nexus connection settings"
    )

    /**
      * Arguments for nexusExpireProxyCache task.
      *
      * 
      * Uses the following system properties by default:
      * - `nexustasks.target` - a name of repository/group to update.
      *   If this property is missing, `nexusExpireProxyCacheArgs`
      *   defaults to None.
      * - `nexustasks.domain` - a domain type (repository or group).
      *   Alowed values are `repository` and `group`. If this property
      *   is missing, domain defaults to [[Domain.Repository]].
      */
    val nexusExpireProxyCacheArgs = SettingKey[Option[ExpireProxyCacheArgs]](
      "nexusExpireProxyCacheArgs",
      "Arguments for nexusExpireProxyCache task"
    )

    /**
      * Expire Nexus cache for project's package.
      *
      * Uses `nexusSettings` and `nexusExpireProxyCacheArgs`, scoped
      * to `nexusExpireProxyCache`. If either is `None`, no update
      * is performed.
      */
    val nexusExpireProxyCache = TaskKey[Unit](
      "nexusExpireProxyCache",
      "Expire cache of Nexus proxy repository for this package"
    )
  }

  import autoImport._

  override lazy val buildSettings = Seq(
    // The settings can be provided via
    nexusSettings := {
      for {
        root <- sysProp("nexustasks.url").map(url)
        user <- sysProp("nexustasks.username")
        pass <- sysProp("nexustasks.password")
      } yield NexusSettings(root, NexusCredentials(user, pass))
    },

    nexusExpireProxyCacheArgs := {
      val domain = sysProp("nexustasks.domain")
        .map(parseDomain)
        .getOrElse(Domain.Repository)

      sysProp("nexustasks.target")
        .map(ExpireProxyCacheArgs(domain, _))
    }
  )

  override lazy val projectSettings = Seq(
    nexusExpireProxyCache in Compile := {

      val settingsOpt = (nexusSettings in (Compile, nexusExpireProxyCache)).value
      val argsOpt = (nexusExpireProxyCacheArgs in (Compile, nexusExpireProxyCache)).value

      val group = organization.value
      val log = streams.value.log
      val artifactId = ivyModule.value.moduleDescriptor(log)
        .getModuleRevisionId()
        .getName
      val ver = version.value

      (settingsOpt, argsOpt) match {
        case (None, _) =>
          // We don't throw if this task is called without necessary
          // configuration
          log.warn("nexusSettings is None, Nexus was not updated")
        case (_, None) =>
          log.warn("nexusExpireProxyCacheArgs is None, Nexus was not updated")
        case (Some(settings), Some(args)) =>
          expireProxyCache(
            settings, args, group, artifactId, ver, log
          )
      }
    }
  )

  private def sysProp(key: String) = Option(System.getProperty(key)).filter(_.nonEmpty)

  private def parseDomain(s: String) = s match {
    case "repository" => Domain.Repository
    case "group" => Domain.Group
    case _ => throw new IllegalArgumentException(
      s"""Illegal value for nexustasks.domain: $s. Allowed values: repository, group."""
    )
  }
}
