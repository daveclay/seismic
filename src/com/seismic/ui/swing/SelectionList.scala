package com.seismic.ui.swing

import java.awt.event.{FocusEvent, FocusListener, KeyEvent, KeyListener}
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._

trait Selectable {
  def name: String
}

class SelectionList[T <: Selectable](onSelectNext: () => Unit,
                                     onSelectPrevious: () => Unit,
                                     onClicked: (T) => Unit,
                                     onAccept: () => Unit,
                                     onBackout: () => Unit,
                                     onAddItem: () => Unit,
                                     backgroundColor: Color) extends JPanel() {

  SwingComponents.addBorder(this)
  setPreferredSize(new Dimension(250, 400))
  setBackground(backgroundColor)
  setFocusable(true)

  var addItemButton = SwingComponents.button("Add")
  addItemButton.addActionListener(e => { onAddItem() })

  var selectionItemsOpt: Option[Seq[SelectionItem[T]]] = None

  addKeyListener(new KeyListener {

    override def keyTyped(e: KeyEvent): Unit = {}
    override def keyReleased(e: KeyEvent): Unit = {}
    override def keyPressed(e: KeyEvent): Unit = {
      val code = e.getKeyCode

      if (code == KeyEvent.VK_UP || code == KeyEvent.VK_KP_UP) {
        onSelectPrevious()
      } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_KP_DOWN) {
        onSelectNext()
      } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_KP_RIGHT) {
        onAccept()
      } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_KP_LEFT) {
        onBackout()
      } else if (code == KeyEvent.VK_SPACE) {
        onAccept()
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

  def setCurrentSelectedItem(item: T): Unit = {
    findSelectionItemFor(item).foreach { selectionItem => selectSelectionItem(selectionItem) }
  }

  private def createSelectItem(item: T) = {
    val itemWasClicked = () => {
      onClicked(item)
    }
    new SelectionItem(item, itemWasClicked)
  }

  private def setAllSelectionItemsHighlightTo(highlight: Highlight): Unit = {
    foreachSelectionItem { selectionItem => selectionItem.setHighlight(highlight) }
  }

  private def selectSelectionItem(selectionItem: SelectionItem[T]): Unit = {
    indicateSelected(selectionItem)
  }

  private def indicateSelected(selectionItem: SelectionItem[T]): Unit = {
    setAllSelectionItemsHighlightTo(Deselected)
    selectionItem.setHighlight(HighSelected)
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
    removeAll()
    selectionItemsOpt.foreach { selectionItems =>
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
