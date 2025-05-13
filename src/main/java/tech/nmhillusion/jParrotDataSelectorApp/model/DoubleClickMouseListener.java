package tech.nmhillusion.jParrotDataSelectorApp.model;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-13
 */
public abstract class DoubleClickMouseListener implements MouseListener {

    public abstract void onDoubleClick(MouseEvent e);

    @Override
    public void mouseClicked(MouseEvent e) {
        if (2 == e.getClickCount()) {
            onDoubleClick(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
