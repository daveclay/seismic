package com.seismic.ui.swing.draglist;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

//http://docs.oracle.com/javase/tutorial/uiswing/dnd/dropmodedemo.html
public class ListItemTransferHandler extends TransferHandler {

    private final DataFlavor localObjectFlavor;
    private int[] indiciesOfMovedItemsToRemove = null;
    private int moveToIndex = -1; //Location where items were added
    private int numberOfMovedValues = 0;  //Number of items added.

    public ListItemTransferHandler() {
        localObjectFlavor = new ActivationDataFlavor(
                Object[].class, DataFlavor.javaJVMLocalObjectMimeType,
                "Array of items");
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JList list = (JList) c;
        indiciesOfMovedItemsToRemove = list.getSelectedIndices();
        Object[] transferedObjects = list.getSelectedValues();
        return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
    }

    @Override
    public boolean canImport(TransferSupport info) {
        if (!info.isDrop() || !info.isDataFlavorSupported(localObjectFlavor)) {
            return false;
        }
        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }

    @Override
    public boolean importData(TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        JList list = (JList) info.getComponent();
        DefaultListModel listModel = (DefaultListModel) list.getModel();
        int targetIndex = findTargetIndexForMovedItems(info, listModel);
        Object[] values = getValues(info);

        moveSelectedItemsToNewIndex(targetIndex, list, values, listModel);
        return true;
    }

    private void moveSelectedItemsToNewIndex(int targetIndex,
                                             JList jlist,
                                             Object[] reorderedItems,
                                             DefaultListModel listModel) {
        moveToIndex = targetIndex;
        numberOfMovedValues = reorderedItems.length;
        for (Object reorderedItem : reorderedItems) {
            int nextTargetIndex = targetIndex++;
            listModel.add(nextTargetIndex, reorderedItem);
            jlist.addSelectionInterval(nextTargetIndex, nextTargetIndex);
        }
    }

    private Object[] getValues(TransferSupport info) {
        try {
            return (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
        } catch (UnsupportedFlavorException | IOException ufe) {
            throw new IllegalStateException(ufe);
        }
    }

    private int findTargetIndexForMovedItems(TransferSupport info, DefaultListModel listModel) {
        JList.DropLocation dropLocation = (JList.DropLocation) info.getDropLocation();
        int index = dropLocation.getIndex();
        int max = listModel.getSize();
        if (index < 0 || index > max) {
            index = max;
        }
        return index;
    }

    @Override
    protected void exportDone(JComponent component, Transferable data, int action) {
        if (action == MOVE && indiciesOfMovedItemsToRemove != null) {
            removeMovedItemsFromOriginalIndicies(component);
        }
        indiciesOfMovedItemsToRemove = null;
        numberOfMovedValues = 0;
        moveToIndex = -1;
    }

    private void removeMovedItemsFromOriginalIndicies(JComponent component) {
        JList source = (JList) component;
        DefaultListModel model = (DefaultListModel) source.getModel();
        if (numberOfMovedValues > 0) {
            updateIndicesToRemove();
        }
        removeMovedItemsFromOriginalIndicies(model);
    }

    private void removeMovedItemsFromOriginalIndicies(DefaultListModel model) {
        for (int i = indiciesOfMovedItemsToRemove.length - 1; i >= 0; i--) {
            model.remove(indiciesOfMovedItemsToRemove[i]);
        }
    }

    private void updateIndicesToRemove() {
        // https://github.com/aterai/java-swing-tips/blob/master/DnDReorderList/src/java/example/MainPanel.java
        for (int i = 0; i < indiciesOfMovedItemsToRemove.length; i++) {
            if (indiciesOfMovedItemsToRemove[i] >= moveToIndex) {
                indiciesOfMovedItemsToRemove[i] += numberOfMovedValues;
            }
        }
    }
}
