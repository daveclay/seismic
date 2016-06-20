package com.seismic.utils

import java.io.File

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.seismic.SetList

object SetListSerializer {

  def write(setList: SetList): Unit = {
    objectMapper().writeValue(new File(f"${setList.name}.json"), setList)
  }

  def read(file: File) = {
    objectMapper().readValue(file, classOf[SetList])
  }

  private def objectMapper() = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
  }

}
