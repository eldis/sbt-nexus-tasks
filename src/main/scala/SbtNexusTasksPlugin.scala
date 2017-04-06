package sbtnexustasks

import sbt._
import sbt.Keys._

import sbtnexustasks.impl._

object SbtNexusTasksPlugin extends AutoPlugin {

  object autoImport {

    /**
      * Nexus root url.
      *
      * Defaults to `nexustasks.url` system property if it's present.
      */
    val nexusUrl = SettingKey[Option[URL]](
      "nexusUrl",
      "Nexus root url"
    )

    /**
      * Nexus repository to update.
      *
      * Defaults to `nexustasks.repository` system property if it's present.
      */
    val nexusRepository = SettingKey[Option[String]](
      "nexusRepository",
      "Nexus repository url"
    )

    /**
      * Nexus credentials.
      *
      * Defaults to `nexustasks.username` and `nexustasks.password`
      * system properties if both are present.
      */
    val nexusCredentials = SettingKey[Option[NexusCredentials]](
      "nexusCredentials",
      "Nexus username and password"
    )

    /**
      * Expire Nexus cache for project's package.
      */
    val nexusExpireProxyCache = TaskKey[Unit](
      "nexusExpireProxyCache",
      "Expire cache of Nexus proxy repository for this package"
    )
  }

  import autoImport._

  override lazy val buildSettings = Seq(
    // The settings can be provided via
    nexusUrl := sysProp("nexustasks.url").map(url),
    nexusRepository := sysProp("nexustasks.repository"),
    nexusCredentials := {
      for {
        user <- sysProp("nexustasks.username")
        pass <- sysProp("nexustasks.password")
      } yield NexusCredentials(user, pass)
    }
  )

  override lazy val projectSettings = Seq(
    nexusExpireProxyCache := {
      val urlOpt = nexusUrl.value
      val repoOpt = nexusRepository.value
      val credentialsOpt = nexusCredentials.value
      val group = organization.value

      val log = streams.value.log
      val art = ivyModule.value.moduleDescriptor(log)
        .getModuleRevisionId()
        .getName

      (urlOpt, repoOpt, credentialsOpt) match {
        case (None, _, _) =>
          // We don't throw if this task is called without necessary
          // configuration
          log.warn("nexusUrl is not defined, Nexus was not updated")
        case (_, None, _) =>
          log.warn("nexusRepository is not defined, Nexus was not updated")
        case (_, _, None) =>
          log.warn("nexusCredentials is not defined, Nexus was not updated")
        case (Some(url), Some(repo), Some(creds)) =>
          expireProxyCache(
            url, Domain.Repositories, repo, group, art, creds, log
          )
      }
    }
  )

  private def sysProp(key: String) = Option(System.getProperty(key))
}
