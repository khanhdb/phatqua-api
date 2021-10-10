name := "chickenstar"
 
version := "1.0" 
      
lazy val `chickenstar` = (project in file(".")).enablePlugins(PlayScala)

      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.13.5"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )


libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "8.0.22",
  "org.playframework.anorm" %% "anorm" % "2.6.10",
  "com.github.t3hnar" %% "scala-bcrypt" % "4.1"
)
