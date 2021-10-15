name := "chickenstar"
 
version := "1.1"

val dockerSettings = Seq(
  maintainer := "khanhdb@sandinh.net",
  dockerBaseImage := "openjdk:8",
  dockerRepository := Some("r.bennuoc.com")
)
      
lazy val `chickenstar` = (project in file("."))
  .enablePlugins(PlayScala, DockerPlugin)
  .settings(dockerSettings)

      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.5"

libraryDependencies ++= Seq(evolutions, jdbc , ehcache , ws , specs2 % Test , guice )


libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "8.0.22",
  "org.playframework.anorm" %% "anorm" % "2.6.10",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.1"
)
