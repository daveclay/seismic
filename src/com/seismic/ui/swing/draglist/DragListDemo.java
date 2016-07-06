package com.seismic.ui.swing.draglist;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public class DragListDemo {

    public static void main(String[] args) {
        EventQueue.invokeLater(DragListDemo::createAndShowGUI);
    }

    private JComponent makeUI() {

        DefaultListModel<String> m = new DefaultListModel<>();

        JList<String> list = new JList<>(m);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setTransferHandler(new ListItemTransferHandler());
        list.setDropMode(DropMode.INSERT);
        list.setDragEnabled(true);
        //http://java-swing-tips.blogspot.jp/2008/10/rubber-band-selection-drag-and-drop.html
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(0);
        list.setFixedCellWidth(100);
        list.setFixedCellHeight(30);
        list.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        list.setCellRenderer(new ListCellRenderer<String>() {
            private final JPanel p = new JPanel(new BorderLayout());
            private final JLabel label = new JLabel("", JLabel.CENTER);

            @Override
            public Component getListCellRendererComponent(JList list,
                                                          String value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                label.setText(value);
                label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                p.add(label, BorderLayout.SOUTH);
                p.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                return p;
            }
        });

        Arrays.asList("error", "information", "question", "warning").forEach(m::addElement);

        return new JScrollPane(list);
    }

    private static void createAndShowGUI() {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.getContentPane().add(new DragListDemo().makeUI());
        f.setSize(320, 240);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

}
