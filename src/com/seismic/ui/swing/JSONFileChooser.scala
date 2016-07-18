package com.seismic.ui.swing

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.{JFileChooser, JFrame}

import com.seismic.io.Preferences.getPreferences
import com.seismic.io.{Preferences, SetListSerializer}

class JSONFileChooser(frame: JFrame, fileSelected: (File) => Unit) {

  def show(): Unit = {
    val chooser = new JFileChooser(getPreferences.lastSetListDir)
    chooser.setFileFilter(new FileNameExtensionFilter("JSON Files", "json"))
    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      fileSelected(chooser.getSelectedFile)
    }
  }
}
