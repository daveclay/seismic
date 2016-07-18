package com.seismic.io

import java.io.File
import com.seismic.io.Preferences.getPreferences
import com.seismic.io.ObjectMapperFactory.objectMapper
import com.seismic.SetList

object SetListSerializer {

  def main(args: Array[String]): Unit = {
    val setList = read(new File(Preferences.getPreferences.lastSetListDir, "Test.json"))
    println(setList.songs.head.setList)
  }

  def write(setList: SetList): Unit = {
    objectMapper.writeValue(new File(f"${setList.name}.json"), setList)
  }

  def read(file: File) = {
    getPreferences.setLastSetListDirFromFile(file)
    objectMapper.readValue(file, classOf[SetList])
  }
}
