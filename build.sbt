name := "ReaderWriterStateBenchmarks"

version := "0.1"

scalaVersion := "2.13.7"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core"   % "2.7.0",
  "org.typelevel" %% "cats-effect" % "3.3.5",
  "org.typelevel" %% "cats-mtl"    % "1.2.1",
  "dev.zio"       %% "zio"         % "1.0.13",
  "dev.zio"       %% "zio-prelude" % "1.0.0-RC8",
  "org.atnos"     %% "eff"         % "5.23.0"
)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")

enablePlugins(JmhPlugin)
