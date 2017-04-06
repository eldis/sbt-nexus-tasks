package sbtnexustasks

import java.net.URL
import sbt._

import scalaj.http._

package object impl {

  /**
    * Expire an artifact in Nexus proxy cache.
    */
  def expireProxyCache(
    root: URL,
    domain: Domain,
    target: String,
    group: String,
    artifact: String,
    credentials: NexusCredentials,
    log: Logger
  ) = {
    assert(!target.contains("/"))
    val pathFragments = Seq(
      "service/local/data_cache",
      domainToString(domain),
      target,
      "content",
      toPathFragment(group),
      artifact
    )
    val url = pathFragments.foldLeft(root)(combineUrl)
    log.info("Expiring on URL: " + url)

    val resp = Http(url.toString)
      .method("DELETE")
      .auth(credentials.username, credentials.password)
      .timeout(3000, 30000)
      .asString
      .throwError
  }

  private def domainToString(d: Domain): String = d match {
    case Domain.Repositories => "repositories"
    case Domain.RepoGroups => "repo_groups"
  }

  private def toPathFragment(group: String): String =
    group.replace('.', '/')

  private def combineUrl(url: URL, path: String): URL = {
    val s = url.toString()
    val separator = if (s.endsWith("/")) "" else "/"
    new URL(s + separator + path)
  }
}
