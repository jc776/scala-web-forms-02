enablePlugins(ScalaJSPlugin, WorkbenchPlugin)

name := "Example"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots") 
)

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.1",
  "com.lihaoyi" %%% "scalatags" % "0.6.2",
  "com.marekkadek" %%% "scalatags-vdom" % "0.4.0-SNAPSHOT",
  "com.chuusai" %%% "shapeless" % "2.3.2",
  "com.lihaoyi" %%% "upickle" % "0.4.3",
  "com.lihaoyi" %%% "pprint" % "0.4.3"
)

jsDependencies ++= Seq(
	ProvidedJS / "morphdom-wzrd.js"
)

scalacOptions ++= Seq("-feature")

// Don't trigger this - it refreshes even if compile fails.
// Now, ~;fastOptJS;refreshBrowsers should do it
// Plus, helps add refresh to backend later
refreshBrowsers <<= refreshBrowsers.triggeredBy()
