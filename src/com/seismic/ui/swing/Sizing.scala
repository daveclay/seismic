package com.seismic.ui.swing

import java.awt.{Container, Dimension}

object Sizing {
  def fitWidth(components: Container*) = {
    components.foldLeft(0) { (width, component) => component.getPreferredSize.width + width }
  }

  def fitHeight(components: Container*) = {
    components.foldLeft(0) { (height, component) => component.getPreferredSize.width + height }
  }

  implicit def extendDimension(dimension: Dimension):ExtendedDimension = new ExtendedDimension(dimension)

  class ExtendedDimension(dimension: Dimension) {

    def increaseSize(amount: Int) = {
      new Dimension(dimension.getWidth.toInt + amount, dimension.getHeight.toInt + amount)
    }

    def increaseWidth(amount: Int) = {
      new Dimension(dimension.getWidth.toInt + amount, dimension.getHeight.toInt)
    }

    def increaseHeight(amount: Int) = {
      new Dimension(dimension.getWidth.toInt, dimension.getHeight.toInt + amount)
    }
  }
}
