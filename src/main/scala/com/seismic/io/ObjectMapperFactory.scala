package com.seismic.io

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object ObjectMapperFactory {
  val objectMapper = buildObjectMapper()

  private def buildObjectMapper() = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper
  }
}
