package com.seismic.ui.swing

import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._

trait Selectable {
  def name: String
}

class SelectionList[T <: Selectable](onItemSelected: (T) => Unit,
                                     onEditItemSelected: (T) => Unit,
                                     onAddItemSelected: () => Unit,
                                     backgroundColor: Color) extends JPanel() {

  SwingComponents.addBorder(this)
  setPreferredSize(new Dimension(250, 400))
  setBackground(backgroundColor)

  var addItemButton = SwingComponents.button("Add")
  addItemButton.addActionListener(e => {onAddItemSelected() })
  var selectionItemsOpt: Option[Seq[SelectionItem[T]]] = None

  def addItem(item: T): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
      val selectItem = createSelectItem(item)
      val all = selectionItems :+ selectItem
      selectionItemsOpt = Option(all)
      layoutSelectionItems()
      indicateSelectedItem(selectItem)
    }
  }

  def setItems(items: Seq[T]): Unit = {
    val selectItems = items.map { item => createSelectItem(item) }
    selectionItemsOpt = Option(selectItems)
    layoutSelectionItems()
  }

  def applySelectionItemFor(item: T, f: (SelectionItem[T]) => Unit) = {
    () => findSelectionItemFor(item).foreach { selectionItem => f(selectionItem) }
  }

  private def indicateSelectedItem(selectionItem: SelectionItem[T]): Unit = {
    foreachSelectionItem { selectionItem => selectionItem.indicateUnselect() }
    selectionItem.indicateSelected()
  }

  private def indicateSelectedItem(item: T): Unit = {
    findSelectionItemFor(item).foreach { selectionItem => indicateSelectedItem(selectionItem) }
  }

  private def createSelectItem(item: T) = {
    val selectPrevious = applySelectionItemFor(item, { selectionItem => selectPreviousFrom(selectionItem) })
    val selectNext = applySelectionItemFor(item, { selectionItem => selectNextFrom(selectionItem) })
    val onShowPhrase = () => {
      onItemSelected(item)
      indicateSelectedItem(item)
    }
    val onEditPhrase = () => {
      onEditItemSelected(item)
      indicateSelectedItem(item)
    }
    new SelectionItem(item, onShowPhrase, onEditPhrase, selectPrevious, selectNext)
  }

  def itemWasUpdated(updatedItem: T): Unit = {
    findSelectionItemFor(updatedItem) match {
      case Some(selectionItem) => selectionItem.setLabel(updatedItem.name)
      case None => println(s"Could not find item to update")
    }
  }

  private def selectPreviousFrom(selectionItem: SelectionItem[T]): Unit = {
    selectionItemsOpt.foreach { selectionITems =>
      val index = selectionITems.indexOf(selectionItem)
      selectItemAt(wrapIndex(index - 1, selectionITems))
    }
  }

  private def selectNextFrom(selectionItem: SelectionItem[T]): Unit = {
    selectionItemsOpt.foreach { selectionITems =>
      val index = selectionITems.indexOf(selectionItem)
      selectItemAt(wrapIndex(index + 1, selectionITems))
    }
  }

  private def wrapIndex(index: Int, array: Seq[Any]) = {
    if (index < 0) {
      array.size - 1
    } else if (index >= array.size) {
      0
    } else {
      index
    }
  }

  private def selectFirst(): Unit = {
    selectionItemsOpt.foreach { selectionITems =>
      selectionITems.head.grabFocus()
    }
  }

  private def selectLast(): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
      selectionItems.last.grabFocus()
    }
  }

  private def selectItemAt(index: Int): Unit = {
    selectionItemsOpt.foreach { selectionItems =>
      selectItem(selectionItems(index))
    }
  }

  private def selectItem(item: SelectionItem[T]): Unit = {
    item.grabFocus()
    indicateSelectedItem(item)
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
