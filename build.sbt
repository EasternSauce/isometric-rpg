ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "untitled1"
  )
val libGdxVersion = "1.12.1"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies += "com.badlogicgames.gdx" % "gdx" % libGdxVersion
libraryDependencies += "com.badlogicgames.gdx" % "gdx-box2d" % libGdxVersion
libraryDependencies += "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % libGdxVersion
libraryDependencies += "com.badlogicgames.gdx" % "gdx-freetype" % libGdxVersion

libraryDependencies += "com.badlogicgames.gdx" % "gdx-platform" % libGdxVersion classifier "natives-desktop"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-box2d-platform" % libGdxVersion classifier "natives-desktop"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-freetype-platform" % libGdxVersion classifier "natives-desktop"

libraryDependencies += "com.softwaremill.quicklens" %% "quicklens" % "1.9.0"

libraryDependencies += "com.esotericsoftware" % "kryonet" % "2.22.0-RC1"

libraryDependencies += "space.earlygrey" % "shapedrawer" % "2.4.0"

libraryDependencies += "com.twitter" %% "chill" % "0.10.0"

libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0"