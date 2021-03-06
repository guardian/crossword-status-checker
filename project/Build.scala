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

  val awsVersion = "1.11.280"

  val root = Project("crossword-status-checker", file("."))
    .settings(

      libraryDependencies ++= Seq(
        "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
        "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
        "com.amazonaws" % "aws-java-sdk-sns" % awsVersion,
        "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
        "com.squareup.okhttp3" % "okhttp" % "3.9.0",
        "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
        "com.google.guava" % "guava" % "18.0",
        "com.gu" %% "content-api-client-aws" % "0.5",
        "org.scalatest" %% "scalatest" % "2.2.5" % "test",
        "org.json4s" % "json4s-native_2.11" % "3.4.0"
      )
    )
    .settings(basicSettings)
    .settings(scalariformSettings)

}