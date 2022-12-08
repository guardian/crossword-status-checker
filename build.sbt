import sbt._
import Keys._

val basicSettings = Seq(
  organization  := "com.gu",
  description   := "AWS Lambda to check crossword status.",
  scalaVersion  := "2.11.7",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  assemblyJarName := "crossword-status-checker.jar",
  assembly / test := (Test / test).value,
  assembly / assemblyMergeStrategy := {
    case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
    case path => MergeStrategy.defaultMergeStrategy(path)
  }
)

val awsVersion = "1.11.280"

val root = Project("crossword-status-checker", file("."))
  .settings(

    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
      "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-sns" % awsVersion,
      "com.amazonaws" % "aws-java-sdk-sts" % awsVersion,
      "com.squareup.okhttp3" % "okhttp" % "4.10.0",
      "com.google.guava" % "guava" % "18.0",
      "com.gu" %% "content-api-client-aws" % "0.7",
      "org.scalatest" %% "scalatest" % "3.2.14" % "test",
      "org.json4s" %% "json4s-native" % "4.0.6"
    )
  )
  .settings(basicSettings)

