package com.seismic.ui.swing

import javax.swing.SwingUtilities

object SwingThreadHelper {
  def invokeLater(f: () => Unit): Unit = {
    SwingUtilities.invokeLater(new Runnable() {
      def run(): Unit = f()
    })
  }
}
