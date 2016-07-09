package com.seismic.ui.swing.draglist

import java.awt.event._
import javax.swing._
import java.awt._
import javax.swing.event.{ListDataEvent, ListDataListener, ListSelectionEvent, ListSelectionListener}

import com.daveclay.swing.util.Position.position
import com.seismic.ui.swing.{Sizing, SwingComponents}

case class ListCallbacks[T](onSelected: (T) => Unit,
                            onAccept: () => Unit,
                            onBackout: () => Unit,
                            onAddItem: () => Unit,
                            onReordered: (Seq[T]) => Unit,
                            onSelectAfterLast: () => Unit = null,
                            onSelectBeforeFirst: () => Unit = null)

class OrderableSelectionList[T](callbacks: ListCallbacks[T],
                                renderItem: (T, CellState) => Component) extends JPanel {

  trait SelectionItem[T] {
    def triggerSelected(): Unit
    def triggerAccept()
    def renderCell(cellState: CellState): Component
  }

  case class ValueSelectionItem(value: T) extends SelectionItem[T] {
    def renderCell(cellState: CellState) = {
      renderItem(value, cellState)
    }
    def triggerSelected(): Unit = {
      callbacks.onSelected(value)
    }
    def triggerAccept(): Unit = {
      callbacks.onAccept()
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
    def triggerSelected(): Unit = {
      // TODO: goddamnit, this happens from navigation OR click. fucking swing.
    }
    def triggerAccept(): Unit = {
      callbacks.onAddItem()
    }
  }

  SwingComponents.addBorder(this)
  setFocusable(true)

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
  jlist.addKeyListener(new SelectionListKeyAdapter)
  jlist.addListSelectionListener((e: ListSelectionEvent) => {
    if (jlist.getSelectedValue != null) {
      println(f"list selection event ====> ${jlist.getSelectedValue}")
      fireCurrentSelection(fromMouse = false)
    }
  })
  // TODO: configurable ui
  jlist.setVisibleRowCount(0)

  val onReorderedItems = (selectionItems: Seq[SelectionItem[T]]) => {
    val values = selectionItems.flatMap {
      case ValueSelectionItem(value) => Some(value)
      case _ => None
    }
    callbacks.onReordered(values)
  }
  // Apparently, the magic bullshit happens here.
  jlist.setTransferHandler(new ListItemTransferHandler[SelectionItem[T]](onReorderedItems))

  val scrollPane = new JScrollPane(jlist)
  scrollPane.setOpaque(false)

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

  private def fireCurrentSelection(fromMouse: Boolean): Unit = {
    val selectedValue = jlist.getSelectedValue
    if (selectedValue == addButtonItem && fromMouse) {
      selectedValue.triggerAccept()
    } else {
      if (!lastSelectedOpt.contains(selectedValue)) {
        lastSelectedOpt = Option(selectedValue)
        selectedValue.triggerSelected()
      }
    }
  }

  private def fireAccept(): Unit = {
    val selectedValue = jlist.getSelectedValue
    if (selectedValue != null) {
      // ugh, the fuck?
      selectedValue.triggerAccept()
    }
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
        val button = addButtonItem.button
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
        println(f"mouse clicked event ====> ${jlist.getSelectedValue}")
        fireCurrentSelection(fromMouse = true)
      }
    }

    private def isMenuShortcutKeyDown(event: InputEvent) = {
      (event.getModifiers & Toolkit.getDefaultToolkit.getMenuShortcutKeyMask) != 0
    }
  }

  class SelectionListKeyAdapter extends KeyAdapter {

    override def keyPressed(e: KeyEvent){
      val code = e.getKeyCode
      if (code == KeyEvent.VK_ENTER
        || code == KeyEvent.VK_SPACE
        || code == KeyEvent.VK_RIGHT
        || code == KeyEvent.VK_KP_RIGHT) {
        fireAccept()
      } else if (code == KeyEvent.VK_LEFT
        || code == KeyEvent.VK_KP_LEFT) {
        callbacks.onBackout()
      } else if (code == KeyEvent.VK_UP
        || code == KeyEvent.VK_KP_UP) {
        if (jlist.getSelectedIndex == 0) {
          e.consume()
          if (callbacks.onSelectBeforeFirst != null) {
            callbacks.onSelectBeforeFirst()
          } else {
            jlist.setSelectedIndex(listModel.getSize - 1)
          }
        }
      } else if (code == KeyEvent.VK_DOWN
        || code == KeyEvent.VK_KP_DOWN) {
        if (jlist.getSelectedIndex == listModel.getSize - 1) {
          e.consume()
          if (callbacks.onSelectAfterLast != null) {
            callbacks.onSelectAfterLast()
          } else {
            jlist.setSelectedIndex(0)
          }
        }
      }
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

