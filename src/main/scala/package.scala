package sbtnexustasks

import java.net.URL

sealed trait Domain

object Domain {
  object Repository extends Domain
  object Group extends Domain
}

/**
  * Nexus connection credentials.
  */
case class NexusCredentials(username: String, password: String)

/**
  * Nexus connection settings, common for all tasks.
  */
case class NexusSettings(
  url: URL,
  credentials: NexusCredentials
)

/**
  * Arguments for [[SbtNexusTasksPlugin.autoImport.nexusExpireProxyCache]] task.
  */
case class ExpireProxyCacheArgs (
  domain: Domain,
  target: String
)
