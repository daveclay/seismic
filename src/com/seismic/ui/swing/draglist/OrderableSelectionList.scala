package com.seismic.ui.swing.draglist

import java.awt.event._
import javax.swing._
import java.awt._
import javax.swing.event.{ListDataEvent, ListDataListener, ListSelectionEvent, ListSelectionListener}

import com.daveclay.swing.util.Position.position
import com.seismic.ui.swing.{Sizing, SwingComponents}

case class ListCallbacks[T](onClick: (T) => Unit,
                            onBackout: () => Unit,
                            onAddItem: () => Unit,
                            onReordered: (Seq[T]) => Unit)

class OrderableSelectionList[T](callbacks: ListCallbacks[T],
                                renderItem: (T, CellState) => Component) extends JPanel {

  trait SelectionItem[E] {
    def triggerClicked()
    def renderCell(cellState: CellState): Component
  }

  case class ValueSelectionItem(value: T) extends SelectionItem[T] {
    def renderCell(cellState: CellState) = {
      renderItem(value, cellState)
    }
    def triggerClicked(): Unit = {
      callbacks.onClick(value)
    }
  }

  case class AddButtonItem() extends SelectionItem[T] {
    val button = SwingComponents.button("Add")
    def renderCell(cellState: CellState) = {
      if (cellState.cellHasFocus) {
        SwingComponents.buttonFocused(button)
      } else {
        SwingComponents.buttonBlurred(button)
      }
      button
    }
    def triggerSelected(): Unit = {}
    def triggerClicked(): Unit = {
      callbacks.onAddItem()
    }
  }

  SwingComponents.addBorder(this)
  setFocusable(false)

  var lastSelectedOpt: Option[SelectionItem[T]] = None
  val listModel = new DefaultListModel[SelectionItem[T]]
  val jlist = new NotFuckingStupidJList[SelectionItem[T]](listModel)
  val addButtonItem = new AddButtonItem
  jlist.getSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
  jlist.setDropMode(DropMode.INSERT)
  jlist.setDragEnabled(true)
  jlist.setLayoutOrientation(JList.VERTICAL)
  jlist.setCellRenderer(new SelectionListItemRenderer)
  jlist.addMouseListener(new SelectionListMouseListener)
  // TODO: configurable ui
  jlist.setVisibleRowCount(0)

  private val onReorderedItems = (selectionItems: Seq[SelectionItem[T]]) => {
    val values = selectionItems.flatMap {
      case ValueSelectionItem(value) => Some(value)
      case _ => None
    }
    callbacks.onReordered(values)
  }

  val isReorderable = (item: SelectionItem[T]) => { item != addButtonItem }
  // Apparently, the magic bullshit happens here.
  private val transferHandler = new ListItemTransferHandler[SelectionItem[T]](onReorderedItems, isReorderable)
  jlist.setTransferHandler(transferHandler)

  val scrollPane = new JScrollPane(jlist)
  scrollPane.setOpaque(false)
  scrollPane.setBorder(BorderFactory.createEmptyBorder())

  override def setPreferredSize(size: Dimension): Unit = {
    import Sizing._
    val innerSize = size.decreaseSize(4)
    super.setPreferredSize(size)
    jlist.setFixedCellWidth(innerSize.width)
    jlist.setFixedCellHeight(30)
    scrollPane.setPreferredSize(size)
    position(scrollPane).atOrigin().in(this)
  }

  override def setBackground(color: Color): Unit = {
    super.setBackground(color)
    if (jlist != null) {
      jlist.setBackground(color)
    }
  }

  override def grabFocus(): Unit = {
    jlist.grabFocus()
    if (jlist.getSelectedIndex < 0) {
      jlist.setSelectedIndex(0)
    }
  }

  override def addKeyListener(keyListener: KeyListener): Unit = {
    super.addKeyListener(keyListener)
    jlist.addKeyListener(keyListener)
  }

  def setItems(values: Seq[T]): Unit = {
    listModel.removeAllElements()
    values.map { value => new ValueSelectionItem(value) }.foreach { item => listModel.addElement(item) }
    listModel.addElement(addButtonItem)
  }

  def setCurrentSelectedItem(item: T) = {
    jlist.setSelectedIndex(listModel.indexOf(ValueSelectionItem(item)))
  }

  def addItem(item: T): Unit = {
    listModel.removeElement(addButtonItem)
    val newItem = new ValueSelectionItem(item)
    listModel.addElement(newItem)
    listModel.addElement(addButtonItem)
    jlist.setSelectedValue(newItem, true)
  }

  def itemWasUpdated(item: T): Unit = {
    jlist.revalidate()
    jlist.repaint()
  }

  class SelectionListItemRenderer extends ListCellRenderer[SelectionItem[T]]() {
    override def getListCellRendererComponent(list: JList[_ <: SelectionItem[T]],
                                              selectionItem: SelectionItem[T],
                                              index: Int,
                                              isSelected: Boolean,
                                              cellHasFocus: Boolean): Component = {

      selectionItem.renderCell(CellState(isSelected, cellHasFocus))
    }
  }

  class SelectionListMouseListener extends MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      val selectedValue = jlist.getSelectedValue
      if (selectedValue == addButtonItem) {
        SwingComponents.buttonFocused(addButtonItem.button)
      }
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      val selectedValue = jlist.getSelectedValue

      if (jlist.locationToIndex(e.getPoint) == -1 && !e.isShiftDown && !isMenuShortcutKeyDown(e)) {
        jlist.clearSelection()
      } else {
        if (selectedValue == addButtonItem) {
          SwingComponents.buttonBlurred(addButtonItem.button)
        }
        selectedValue.triggerClicked()
      }
    }

    private def isMenuShortcutKeyDown(event: InputEvent) = {
      (event.getModifiers & Toolkit.getDefaultToolkit.getMenuShortcutKeyMask) != 0
    }
  }

  class DisablingKeyAdapter extends KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit ={
      e.consume()
    }
  }
}

case class CellState(isSelected: Boolean, cellHasFocus: Boolean)

class NotFuckingStupidJList[T](listModel: ListModel[T]) extends JList[T](listModel) {

  def isEventWithinCell(e: MouseEvent): Boolean = {
    val index = locationToIndex(e.getPoint)
    index > -1 && getCellBounds(index, index).contains(e.getPoint)
  }

  override def processMouseEvent(e: MouseEvent): Unit = {
    if (isEventWithinCell(e)) {
      super.processMouseEvent(e)
    }
  }

  override def processMouseMotionEvent(e: MouseEvent) {
    if (isEventWithinCell(e)) {
      super.processMouseMotionEvent(e)
    }
  }
}


