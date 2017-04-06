package sbtnexustasks

sealed trait Domain

object Domain {
  object Repositories extends Domain
  object RepoGroups extends Domain
}

case class NexusCredentials(username: String, password: String)
