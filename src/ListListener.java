import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.SwingUtilities;

/**
 * Mouse listener for the entry title list.
 *
 * @author Gabor_Bata
 *
 */
class ListListener extends MouseAdapter {

    /**
     * Show entry on double click.
     *
     * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent evt) {
        if (PasswordManagerFrame.getInstance().isProcessing()) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 2) {
            EntryHelper.editEntry(PasswordManagerFrame.getInstance());
        }
    }

    /**
     * Handle pop-up.
     *
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent evt) {
        checkPopup(evt);
    }

    /**
     * Handle pop-up.
     *
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent evt) {
        checkPopup(evt);
    }

    /**
     * Checks pop-up trigger.
     *
     * @param evt mouse event
     */
    private void checkPopup(MouseEvent evt) {
        if (PasswordManagerFrame.getInstance().isProcessing()) {
            return;
        }
        if (evt.isPopupTrigger()) {
            JList list = PasswordManagerFrame.getInstance().getEntryTitleList();
            if (list.isEnabled()) {
                Point point = new Point(evt.getX(), evt.getY());
                list.setSelectedIndex(list.locationToIndex(point));
                PasswordManagerFrame.getInstance().getPopup().show(evt.getComponent(), evt.getX(), evt.getY());
            }
        }
    }
}
