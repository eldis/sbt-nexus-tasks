version := {
  val pluginVersion = System.getProperty("plugin.version")
  if (pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else pluginVersion
}

// This project can be used to expire the artifact for the plugin itself
organization := "com.github.eldis"
name := "sbt-nexus-tasks"

enablePlugins(SbtNexusTasksPlugin)
