package com.seismic.ui.swing

import java.io.File
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.{JFileChooser, JFrame}

class JSONFileChooser(frame: JFrame, fileSelected: (File) => Unit) {

  def show(): Unit = {
    val chooser = new JFileChooser
    val filter = new FileNameExtensionFilter("JSON Files", "json")
    chooser.setFileFilter(filter)
    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      fileSelected(chooser.getSelectedFile)
    }
  }
}
