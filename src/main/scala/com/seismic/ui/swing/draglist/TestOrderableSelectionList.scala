package com.seismic.ui.swing.draglist

import java.awt.{BorderLayout, Color, Dimension}
import javax.swing.{JFrame, JLabel, JPanel, WindowConstants}

import com.seismic.ui.swing.SwingComponents

object TestOrderableSelectionList {

  def main(args: Array[String]) {
    val f = new JFrame()
    f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

    class RunShit {
      val onSelected = (s: String) => println(f"selected: $s")
      val onAccept = (value: String) => println(f"accepted $value")
      val onBackout = () => println(f"backout")
      val renderItem = (value: String, cellState: CellState) => {
        val label = new JLabel
        label.setOpaque(false)
        label.setForeground(new Color(200, 200, 200))

        val p = new JPanel
        if (cellState.cellHasFocus) {
          SwingComponents.buttonFocused(p)
        } else {
          SwingComponents.buttonBlurred(p)
        }

        label.setText(value.toString)
        p.add(label, BorderLayout.SOUTH)
        p
      }

      val onReordered = (strings: Seq[String]) => {
        println("reordered")
      }

      val callbacks = ListCallbacks(onAccept, onBackout, onAddItem, onReordered)
      val list = new OrderableSelectionList[String](callbacks,
                                                     renderItem,
                                                     () => SwingComponents.button("Add"),
                                                     new Color(10, 23, 20))
      list.setItems(Array("Thing A", "Thing B", "Thing C", "D", "Bullshit"))
      list.setPreferredSize(new Dimension(320, 240))

      def getList = list

      def onAddItem(): Unit = {
        list.addItem(Math.random().toString)
      }
    }

    f.getContentPane.add(new RunShit().getList)
    f.setSize(320, 640)
    f.setLocationRelativeTo(null)
    f.setVisible(true)
  }
}
