package com.seismic.ui.utils.layout

import java.awt._
import javax.swing.JPanel

class GridBagLayoutHelper(parent: Container) {
  parent.setLayout(new GridBagLayout)

  case class Location(row: Int, col: Int)

  private def coordinateOf(componentToFind: Component) = {
    val gridBagLayout = parent.getLayout.asInstanceOf[GridBagLayout]
    val constraints = gridBagLayout.getConstraints(componentToFind)
    Location(constraints.gridx, constraints.gridy)
  }

  class AddChild(child: Component) {
    val constraints = new GridBagConstraints()

    def withPadding(padding: Int): AddChild = {
      withPadding(new Insets(padding, padding, padding, padding))
    }

    def withPadding(insets: Insets) = {
      constraints.insets = insets
      this
    }

    def nextTo(other: Component) = {
      val location = coordinateOf(other)
      constraints.gridx = location.row + 1
      constraints.gridy = location.col
      this
    }

    def below(other: Component) = {
      val location = coordinateOf(other)
      constraints.gridx = location.row
      constraints.gridy = location.col + 1
      this
    }

    def atOrigin() = {
      constraints.gridx = 0
      constraints.gridy = 0
      this
    }

    def weightX(weightX: Float) = {
      constraints.weightx = weightX
      this
    }

    def weightY(weightY: Float) = {
      constraints.weighty = weightY
      this
    }

    def colspan(colspan: Int) = {
      constraints.gridwidth = colspan
      this
    }

    def rowspan(rowspan: Int) = {
      constraints.gridheight = rowspan
      this
    }

    def fillHorizontal() = {
      constraints.fill = GridBagConstraints.HORIZONTAL
      this
    }

    def fillVertical() = {
      constraints.fill = GridBagConstraints.VERTICAL
      this
    }

    def fill() = {
      constraints.fill = GridBagConstraints.BOTH
      this
    }

    def align(align: Int) = {
      constraints.anchor = align
      this
    }

    def alignLeft() = {
      constraints.anchor = GridBagConstraints.FIRST_LINE_START
      this
    }

    def alignRight() = {
      constraints.anchor = GridBagConstraints.FIRST_LINE_END
      this
    }

    def inParent(): Unit = {
      parent.add(child, constraints)
    }
  }

  def verticalSpacer(panel: JPanel) = {
    panel.setOpaque(false)
    position(panel).fillVertical().weightY(1).alignLeft()
  }

  def horizontalSpacer(panel: JPanel): AddChild = {
    panel.setOpaque(false)
    position(panel).fillHorizontal().weightX(1).alignLeft()
  }

  def horizontalSpacer(): AddChild = {
    val panel = new JPanel()
    horizontalSpacer(panel)
  }

  def position(child: Component) = {
    new AddChild(child)
  }
}
