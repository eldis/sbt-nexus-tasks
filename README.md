# sbt-nexus-tasks

Tasks for managing Nexus server from SBT.

# Initial setup

Add the following to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.eldis" % "sbt-nexus-tasks" % "0.1.0-SNAPSHOT")
```

## Providing settings via SBT configuraiton

Add the following to your `build.sbt`:

```scala
import sbtnexustasks._

enablePlugins(SbtNexusTasksPlugin)

nexusSettings in ThisBuild := Some(NexusSettings(
  url("http://nexus.example.com/nexus"),
  NexusCredentials("user", "pass1234")
))

nexusExpireProxyCacheArgs in ThisBuild := Some(ExpireProxyCacheArgs(
  Domain.Group,
  "my_repository_group"
))

```

## Providing settings via JVM system properties

An alternative to SBT config is passing JVM system properties. This can be useful in CI scripts:

```
sbt -Dnexustasks.url=http://nexus.example.com/nexus \
    -Dnexustasks.username=user \
    -Dnexustasks.password=pass1234 \
    -Dnexustasks.domain=group \
    -Dnexustasks.target=my_repository_group
```

# Tasks

## Expire Cache (nexusExpireCache task)

Expires the cache on Nexus Proxy repository corresponding to the current module.

It's sometimes useful to trigger it automatically on publish:

```scala
publish in Compile := {
  publish.in(Compile).value
  nexusExpireProxyCache.in(Compile).value
}
```

# License

The MIT License (MIT). Please see [License File](LICENSE.md) for more information.
