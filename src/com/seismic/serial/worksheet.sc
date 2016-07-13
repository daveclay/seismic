import com.fasterxml.jackson.annotation.{JsonBackReference, JsonManagedReference}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.seismic.utils.RandomHelper

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex


val s = "T,ON,KICK,205,1234"
val a = s.split(",")

a.head match {
  case "T" => s"hi ${a.drop(1).mkString(",")}"
  case _ => None
}
