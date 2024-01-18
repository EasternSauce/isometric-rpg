ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "untitled1"
  )
val libGdxVersion = "1.12.1"

libraryDependencies += "com.badlogicgames.gdx" % "gdx" % libGdxVersion
libraryDependencies += "com.badlogicgames.gdx" % "gdx-box2d" % libGdxVersion
libraryDependencies += "com.badlogicgames.gdx" % "gdx-backend-lwjgl3" % libGdxVersion
libraryDependencies += "com.badlogicgames.gdx" % "gdx-freetype" % libGdxVersion

libraryDependencies += "com.badlogicgames.gdx" % "gdx-platform" % libGdxVersion classifier "natives-desktop"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-box2d-platform" % libGdxVersion classifier "natives-desktop"
libraryDependencies += "com.badlogicgames.gdx" % "gdx-freetype-platform" % libGdxVersion classifier "natives-desktop"

libraryDependencies += "com.softwaremill.quicklens" %% "quicklens" % "1.9.0"
