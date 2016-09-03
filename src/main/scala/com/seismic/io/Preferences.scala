package com.seismic.io

import java.io.File

import com.seismic.{HandleCalibration, SetList, TriggerThresholds}
import com.seismic.io.ObjectMapperFactory.objectMapper

object Preferences {
  private val homeDir = System.getProperties.getProperty("user.dir")
  private val preferencesFile = new File(homeDir, ".seismic.json")
  private var preferencesOpt: Option[Preferences] = None

  private def buildPreferences() = {
    if ( ! preferencesFile.exists()) {
      val preferences = Preferences(homeDir)
      objectMapper.writeValue(preferencesFile, preferences)
      preferences
    } else {
      objectMapper.readValue(preferencesFile, classOf[Preferences])
    }
  }

  def getPreferences = {
    preferencesOpt match {
      case Some(preferences) => preferences
      case None =>
        preferencesOpt = Option(buildPreferences())
        preferencesOpt.get
    }
  }

  def main(args: Array[String]): Unit = {
    println(Preferences.getPreferences.handleCalibration)
  }
}

case class Preferences(var lastSetListDir: String,
                       var handleCalibration: HandleCalibration = HandleCalibration(),
                       var triggerThresholds: TriggerThresholds = TriggerThresholds()) {

  if (triggerThresholds == null) {
    triggerThresholds = TriggerThresholds()
    save()
  }

  def save() {
    objectMapper.writeValue(Preferences.preferencesFile, this)
  }

  def setLastSetListDirFromFile(lastSetListFile: File): Unit = {
    this.lastSetListDir = lastSetListFile.getParentFile.getAbsolutePath
    save()
  }
}