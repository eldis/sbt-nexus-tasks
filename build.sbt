organization := "com.github.eldis"
name := "sbt-nexus-tasks"

sbtPlugin := true

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.3.0"

publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
