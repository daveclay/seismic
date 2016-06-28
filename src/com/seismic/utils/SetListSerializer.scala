package com.seismic.utils

import java.io.File

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.seismic.SetList

object SetListSerializer {

  object Preferences {
    val homeDir = System.getProperties.getProperty("user.dir")
    val preferencesFile = new File(homeDir, ".seismic.json")

    def buildPreferences() = {
      if ( ! preferencesFile.exists()) {
        val preferences = Preferences(homeDir)
        objectMapper().writeValue(preferencesFile, preferences)
        preferences
      } else {
        objectMapper().readValue(preferencesFile, classOf[Preferences])
      }
    }
  }

  val preferences = Preferences.buildPreferences()

  case class Preferences(var lastSetListDir: String) {
    def setLastSetListDirFromFile(lastSetListFile: File): Unit = {
      this.lastSetListDir = lastSetListFile.getParentFile.getAbsolutePath
    }
  }

  def write(setList: SetList): Unit = {
    objectMapper().writeValue(new File(f"${setList.name}.json"), setList)
  }

  def read(file: File) = {
    preferences.setLastSetListDirFromFile(file)
    objectMapper().writeValue(Preferences.preferencesFile, preferences)
    objectMapper().readValue(file, classOf[SetList])
  }

  private def objectMapper() = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
  }

}
