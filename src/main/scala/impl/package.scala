package sbtnexustasks

import java.net.URL
import sbt._

import scalaj.http._

package object impl {

  /**
    * Expire an artifact in Nexus proxy cache.
    */
  def expireProxyCache(
    settings: NexusSettings,
    args: ExpireProxyCacheArgs,
    group: String,
    artifact: String,
    version: String,
    log: Logger
  ) = {
    assert(!args.target.contains("/"))

    val NexusSettings(root, NexusCredentials(username, password)) = settings

    val pathFragments = Seq(
      "service/local/data_cache",
      domainToString(args.domain),
      args.target,
      "content",
      toPathFragment(group),
      artifact,
      version
    )
    val url = pathFragments.foldLeft(root)(combineUrl)
    log.info("Expiring on URL: " + url)

    val resp = Http(url.toString)
      .method("DELETE")
      .auth(username, password)
      .timeout(3000, 30000)
      .asString
      .throwError
  }

  private def domainToString(d: Domain): String = d match {
    case Domain.Repository => "repositories"
    case Domain.Group => "repo_groups"
  }

  private def toPathFragment(group: String): String =
    group.replace('.', '/')

  private def combineUrl(url: URL, path: String): URL = {
    val s = url.toString()
    val separator = if (s.endsWith("/")) "" else "/"
    new URL(s + separator + path)
  }
}
