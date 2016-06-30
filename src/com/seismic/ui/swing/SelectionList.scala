package com.seismic.ui.swing

import java.awt.event.{KeyEvent, KeyListener}
import java.awt.{Color, ContainerOrderFocusTraversalPolicy, Dimension}
import javax.swing.{JComponent, JPanel, KeyStroke}

import com.daveclay.swing.util.Position._
import com.seismic.utils.ArrayUtils
import com.seismic.utils.ArrayUtils.wrapIndex

trait Selectable {
  def name: String
}

class SelectionList[T <: Selectable](onItemSelected: (T) => Unit,
                                     onEditItemSelected: (T) => Unit,
                                     onAddItemSelected: () => Unit,
                                     onBackSelected: (T) => Unit,
                                     onNavigatePrevious: () => Unit,
                                     onNavigateNext: () => Unit,
                                     backgroundColor: Color) extends JPanel() {

  SwingComponents.addBorder(this)
  setPreferredSize(new Dimension(250, 400))
  setBackground(backgroundColor)
  setFocusable(true)

  var addItemButton = SwingComponents.button("Add")
  addItemButton.addActionListener(e => {onAddItemSelected() })

  var selectionItemsOpt: Option[Seq[SelectionItem[T]]] = None
  var currentSelectedItemOpt: Option[SelectionItem[T]] = None
  val onEditSelected = () => {
    currentSelectedItemOpt.foreach { selectionItem => onEditItemSelected(selectionItem.item) }
  }

  addKeyListener(new KeyListener {
    override def keyTyped(e: KeyEvent): Unit = {}
    override def keyReleased(e: KeyEvent): Unit = {}
    override def keyPressed(e: KeyEvent): Unit = {
      val code = e.getKeyCode

      if (code == KeyEvent.VK_UP || code == KeyEvent.VK_KP_UP) {
        selectPrevious()
      } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_KP_DOWN) {
        selectNext()
      } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_KP_RIGHT) {
        onEditSelected()
      } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_KP_LEFT) {
        currentSelectedItemOpt.foreach { selectionItem => onBackSelected(selectionItem.item) }
      } else if (code == KeyEvent.VK_SPACE) {
        onEditSelected()
      }
    }
  })

  def addItem(item: T): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
      val selectItem = createSelectItem(item)
      val all = selectionItems :+ selectItem
      selectionItemsOpt = Option(all)
      layoutSelectionItems()
    }
  }

  def setItems(items: Seq[T]): Unit = {
    val selectItems = items.map { item => createSelectItem(item) }
    selectionItemsOpt = Option(selectItems)
    layoutSelectionItems()
  }

  def itemWasUpdated(updatedItem: T): Unit = {
    findSelectionItemFor(updatedItem) match {
      case Some(selectionItem) => selectionItem.setLabel(updatedItem.name)
      case None => println(s"Could not find item to update")
    }
  }

  def selectItem(item: T): Unit = {
    findSelectionItemFor(item).foreach { selectionItem => selectSelectionItem(selectionItem) }
  }

  def deselectAll(): Unit = {
    foreachSelectionItem { selectionItem => selectionItem.indicateUnselect() }
  }

  def indicateSelectedItem(item: T): Unit = {
    deselectAll()
    findSelectionItemFor(item).foreach { selectionItem => indicateSelected(selectionItem) }
  }

  private def createSelectItem(item: T) = {
    val itemWasClicked = () => {
      onItemSelected(item)
    }
    new SelectionItem(item, itemWasClicked)
  }

  private def selectPrevious(): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
    }
    for {
      selectionItems <- selectionItemsOpt
      currentItem <- currentSelectedItemOpt
    } yield {
      val index = selectionItems.indexOf(currentItem)
      if (index == 0) {
        onNavigatePrevious()
      } else {
        selectItemAt(wrapIndex(index - 1, selectionItems))
      }
    }
  }

  private def selectNext(): Unit = {
    for {
      selectionItems <- selectionItemsOpt
      currentItem <- currentSelectedItemOpt
    } yield {
      val index = selectionItems.indexOf(currentItem)
      if (index == selectionItems.size - 1) {
        onNavigateNext()
      } else {
        selectItemAt(wrapIndex(index + 1, selectionItems))
      }
    }
  }

  private def selectItemAt(index: Int): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
      selectSelectionItem(selectionItems(index))
    }
  }

  private def selectSelectionItem(selectionItem: SelectionItem[T]): Unit = {
    indicateSelected(selectionItem)
    currentSelectedItemOpt = Option(selectionItem)
    onItemSelected(selectionItem.item)
  }

  private def indicateSelected(selectionItem: SelectionItem[T]): Unit = {
    deselectAll()
    selectionItem.indicateSelected()
  }

  private def findSelectionItemFor(itemToFind: T) = {
    selectionItemsOpt.flatMap { selectionItems =>
      // TODO: nope, they're case classes, it just does the name equality. two phrases named the same are bad. prevent it.
      selectionItems.find { selectionItem => selectionItem.item.equals(itemToFind) }
    }
  }

  private def foreachSelectionItem(f: (SelectionItem[T]) => Unit): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
      selectionItems.foreach { selectionItem => f(selectionItem) }
    }
  }

  private def layoutSelectionItems(): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
      removeAll()
      val firstSelectionItem = selectionItems.head
      position(firstSelectionItem).atOrigin().in(this)

      val lastSelectionItem = selectionItems.drop(1).foldLeft(firstSelectionItem) { (itemAbove, selectionItem) =>
        position(selectionItem).below(itemAbove).withMargin(4).in(this)
        selectionItem
      }

      position(addItemButton).below(lastSelectionItem).withMargin(4).in(this)
    }
  }
}
