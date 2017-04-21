
lazy val buildSettings = Seq(
  scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),
  initialize :=
    (if (scalaBinaryVersion.value == "2.10") sys.props("scalac.patmat.analysisBudget") = "off"
     else sys.props.remove("scalac.patmat.analysisBudget"))
)

lazy val publishSettings = Seq(
  organization := "com.github.mpetruska",
  name := "one-lensing",
  homepage := Some(url("https://github.com/mpetruska/one-lensing")),
  version := "0.1-SNAPSHOT",
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.php")),
  crossScalaVersions := Seq("2.10.6", "2.11.7", "2.12.1"),
  publishMavenStyle := true,
  pomExtra :=
    <scm>
      <url>git@github.com:mpetruska/one-lensing.git</url>
      <connection>scm:git@github.com:mpetruska/one-lensing.git</connection>
    </scm>
    <developers>
      <developer>
        <id>mpetruska</id>
        <name>Mark Petruska</name>
      </developer>
    </developers>,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false
)

lazy val oneLensing = (project in file("."))
  .settings(buildSettings: _*)
  .settings(publishSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
        "org.scalaz" %% "scalaz-core" % "7.2.11",
        "org.scalatest" %% "scalatest" % "3.0.1" % Test
      )
  )
