import sbt._
import Keys._
import com.typesafe.sbt.SbtScalariform.scalariformSettings

object CrosswordStatusCheckerBuild extends Build {

  val basicSettings = Seq(
    organization  := "com.gu",
    description   := "AWS Lambda to check crossword status.",
    scalaVersion  := "2.11.7",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
  )

  val root = Project("crossword-status-checker", file("."))
    .settings(

      libraryDependencies ++= Seq(
        "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
        "com.amazonaws" % "aws-java-sdk-s3" % "1.10.39",
        "com.amazonaws" % "aws-java-sdk-kinesis" % "1.10.39",
        "com.squareup.okhttp" % "okhttp" % "2.5.0",
        "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
        "com.google.guava" % "guava" % "18.0",
        "org.scalatest" %% "scalatest" % "2.2.5" % "test",
        "net.databinder.dispatch" % "dispatch-core_2.11" % "0.11.3"
      )
    )
    .settings(basicSettings)
    .settings(scalariformSettings)

}