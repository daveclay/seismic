package com.seismic.ui.swing.draglist

import javax.activation.ActivationDataFlavor
import javax.activation.DataHandler
import javax.swing._
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException

class ListItemTransferHandler[T](onReorder: (Seq[T]) => Unit) extends TransferHandler {

  private val localObjectFlavor = new ActivationDataFlavor(classOf[Array[AnyRef]],
                                                            DataFlavor.javaJVMLocalObjectMimeType,
                                                            "Array of items")
  private var indiciesOfMovedItemsToRemove: Array[Int] = null
  private var moveToIndex: Int = -1
  private var numberOfMovedValues: Int = 0

  override protected def createTransferable(c: JComponent): Transferable = {
    val list = c.asInstanceOf[JList[_]]
    indiciesOfMovedItemsToRemove = list.getSelectedIndices
    val transferedObjects = list.getSelectedValues
    new DataHandler(transferedObjects, localObjectFlavor.getMimeType)
  }

  override def canImport(info: TransferHandler.TransferSupport): Boolean = {
    if (!info.isDrop || !info.isDataFlavorSupported(localObjectFlavor)) {
      false
    } else {
      true
    }
  }

  override def getSourceActions(c: JComponent): Int = {
    TransferHandler.MOVE
  }

  override def importData(info: TransferHandler.TransferSupport): Boolean = {
    if (!canImport(info)) {
      return false
    }
    val list = info.getComponent.asInstanceOf[JList[T]]
    val listModel = list.getModel.asInstanceOf[DefaultListModel[T]]
    val targetIndex = findTargetIndexForMovedItems(info, listModel)
    val values = getValues(info)
    moveSelectedItemsToNewIndex(targetIndex, list, values, listModel)
    true
  }

  private def moveSelectedItemsToNewIndex(originalTargetIndex: Int,
                                          jlist: JList[T],
                                          reorderedItems: Array[T],
                                          listModel: DefaultListModel[T]) {
    var targetIndex = originalTargetIndex
    moveToIndex = targetIndex
    numberOfMovedValues = reorderedItems.length
    for (reorderedItem <- reorderedItems) {
      listModel.add(targetIndex, reorderedItem)
      jlist.addSelectionInterval(targetIndex, targetIndex)
      targetIndex = targetIndex + 1
    }
  }

  private def getValues(info: TransferHandler.TransferSupport): Array[T] = {
    try {
      info.getTransferable.getTransferData(localObjectFlavor).asInstanceOf[Array[T]]
    } catch {
      case ufe: Exception => throw new IllegalStateException(ufe)
    }
  }

  private def findTargetIndexForMovedItems(info: TransferHandler.TransferSupport,
                                           listModel: DefaultListModel[_]): Int = {
    val dropLocation = info.getDropLocation.asInstanceOf[JList.DropLocation]
    var index = dropLocation.getIndex
    val max = listModel.getSize
    if (index < 0 || index > max) {
      index = max
    }
    index
  }

  override protected def exportDone(component: JComponent, data: Transferable, action: Int) {
    val model = getDataModel(component)
    if (action == TransferHandler.MOVE && indiciesOfMovedItemsToRemove != null) {
      removeMovedItemsFromOriginalIndicies(model)
      val models = model.toArray.asInstanceOf[Array[T]]
      onReorder(models)
    }
    indiciesOfMovedItemsToRemove = null
    numberOfMovedValues = 0
    moveToIndex = -1
  }

  private def removeMovedItemsFromOriginalIndicies(model: DefaultListModel[_]) {
    if (numberOfMovedValues > 0) {
      updateIndicesToRemove()
    }
    for (i <- (indiciesOfMovedItemsToRemove.length - 1) to 0 by -1) {
      model.remove(indiciesOfMovedItemsToRemove(i))
    }
  }

  private def updateIndicesToRemove() {
    for (i <- indiciesOfMovedItemsToRemove.indices) {
      if (indiciesOfMovedItemsToRemove(i) >= moveToIndex) {
        indiciesOfMovedItemsToRemove(i) += numberOfMovedValues
      }
    }
  }

  private def getDataModel(component: JComponent) = {
    val source = component.asInstanceOf[JList[T]]
    source.getModel.asInstanceOf[DefaultListModel[T]]
  }
}