import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

final class StringUtils {

    private StringUtils() {
        // utility class
    }

    /**
     * This method ensures that the output String has only valid XML unicode characters as specified
     * by the XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the standard</a>. This method
     * will return an empty String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    static String stripNonValidXMLCharacters(final String in) {
        if (in == null || in.isEmpty()) {
            return in;
        }
        StringBuilder out = new StringBuilder();
        char current;
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i);
            if (current == 0x9 || current == 0xA || current == 0xD || current >= 0x20 && current <= 0xD7FF || current >= 0xE000 && current <= 0xFFFD) {
                out.append(current);
            } else {
                out.append('?');
            }
        }
        return out.toString();
    }

    static String stripString(String text) {
        return stripString(text, 80);
    }

    private static String stripString(String text, int length) {
        String result = text;
        if (text != null && text.length() > length) {
            result = text.substring(0, length) + "...";
        }
        return result;
    }

    public static String byteArrayToHex(byte[] array) {
        StringBuilder sb = new StringBuilder(array.length * 2);
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

class SpringUtilities {

    private SpringUtilities() {
        // utility class
    }

    /**
     * Aligns the first {@code rows} * {@code cols} components of {@code parent} in a grid. Each
     * component is as big as the maximum preferred width and height of the components. The parent
     * is made just big enough to fit them all.
     *
     * @param parent parent container
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            return;
        }

        Spring xPadSpring = Spring.constant(xPad);
        Spring yPadSpring = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = rows * cols;

        // Calculate Springs that are the max of the width/height so that all
        // cells have the same size.
        Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
        Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
        for (int i = 1; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

            maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
            maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        }

        // Apply the new width/height Spring. This forces all the
        // components to have the same size.
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

            cons.setWidth(maxWidthSpring);
            cons.setHeight(maxHeightSpring);
        }

        // Then adjust the x/y constraints of all the cells so that they
        // are aligned in a grid.
        SpringLayout.Constraints lastCons = null;
        SpringLayout.Constraints lastRowCons = null;
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
            if (i % cols == 0) { // start of new row
                lastRowCons = lastCons;
                cons.setX(initialXSpring);
            } else { // x position depends on previous component
                cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST), xPadSpring));
            }

            if (i / cols == 0) { // first row
                cons.setY(initialYSpring);
            } else { // y position depends on previous row
                if (lastRowCons != null) {
                    cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH), yPadSpring));
                }
            }
            lastCons = cons;
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        if (lastCons != null) {
            pCons.setConstraint(SpringLayout.SOUTH,
                    Spring.sum(Spring.constant(yPad), lastCons.getConstraint(SpringLayout.SOUTH)));
        }
        if (lastCons != null) {
            pCons.setConstraint(SpringLayout.EAST,
                    Spring.sum(Spring.constant(xPad), lastCons.getConstraint(SpringLayout.EAST)));
        }
    }

    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first {@code rows} * {@code cols} components of {@code parent} in a grid. Each
     * component in a column is as wide as the maximum preferred width of the components in that
     * column; height is similarly determined for each row. The parent is made just big enough to
     * fit them all.
     *
     * @param parent parent container
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    static void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY,
                                int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc) {
            return;
        }

        // Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        // Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}

final class CryptUtils {

    private CryptUtils() {
        // utility class
    }

    /**
     * Calculate SHA-256 hash, with 1000 iterations by default (RSA PKCS5).
     *
     * @param text password text
     * @return hash of the password
     * @throws Exception if error occurred
     */
    static byte[] getPKCS5Sha256Hash(final char[] text) throws Exception {
        return getSha256Hash(text, 1000);
    }

    /**
     * Calculate SHA-256 hash.
     *
     * @param text password text
     * @return hash of the password
     * @throws Exception if error occurred
     */
    public static byte[] getSha256Hash(final char[] text) throws Exception {
        return getSha256Hash(text, 0);
    }

    /**
     * Calculate SHA-256 hash.
     *
     * <p>
     * To slow down the computation it is recommended to iterate the hash operation {@code n} times.
     * While hashing the password {@code n} times does slow down hashing for both attackers and
     * typical users, typical users don't really notice it being that hashing is such a small
     * percentage of their total time interacting with the system. On the other hand, an attacker
     * trying to crack passwords spends nearly 100% of their time hashing so hashing {@code n} times
     * gives the appearance of slowing the attacker down by a factor of {@code n} while not
     * noticeably affecting the typical user. A minimum of 1000 operations is recommended in RSA
     * PKCS5 standard.
     *
     * @param text password text
     * @param iteration number of iterations
     * @return hash of the password
     * @throws Exception if error occurred
     */
    private static byte[] getSha256Hash(final char[] text, final int iteration) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.reset();
        // md.update(salt);
        byte[] bytes = new String(text).getBytes(StandardCharsets.UTF_8);
        byte[] digest = md.digest(bytes);
        for (int i = 0; i < iteration; i++) {
            md.reset();
            digest = md.digest(digest);
        }
        return digest;
    }

    /**
     * Get random number generator.
     *
     * <p>
     * It tries to return with a nondeterministic secure random generator first, if it was
     * unsuccessful for some reason, it returns with the uniform random generator.
     * </p>
     *
     * @return the random number generator.
     */
    static Random newRandomNumberGenerator() {
        Random ret;
        try {
            ret = new SecureRandom();
        } catch (Exception e) {
            ret = new Random();
        }
        return ret;
    }
}

final class ClipboardUtils {

    /**
     * Empty clipboard content.
     */
    private static final EmptyClipboardContent EMPTY_CONTENT = new EmptyClipboardContent();

    private ClipboardUtils() {
        // utility class
    }

    /**
     * Sets text to the system clipboard.
     *
     * @param str text
     * @throws Exception when clipboard is not accessible
     */
    static void setClipboardContent(String str) throws Exception {
        if (str == null || str.isEmpty()) {
            clearClipboardContent();
            return;
        }
        try {
            StringSelection selection = new StringSelection(str);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        } catch (Throwable throwable) {
            throw new Exception("Cannot set clipboard content.");
        }
    }

    /**
     * Clears the system clipboard.
     *
     * @throws Exception when clipboard is not accessible
     */
    private static void clearClipboardContent() throws Exception {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(EMPTY_CONTENT, EMPTY_CONTENT);
        } catch (Throwable throwable) {
            throw new Exception("Cannot set clipboard content.");
        }
    }

    /**
     * Get text from system clipboard.
     *
     * @return the text, or {@code null} if there is no content
     */
    static String getClipboardContent() {
        String result = null;
        try {
            Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                result = String.valueOf(contents.getTransferData(DataFlavor.stringFlavor));
            }
        } catch (Throwable throwable) {
            // ignore
        }
        return result == null || result.isEmpty() ? null : result;
    }

    /**
     * Class representing an empty clipboard content. With the help of this class, the content of
     * clipboard can be cleared.
     *
     * @author Gabor_Bata
     *
     */
    static final class EmptyClipboardContent implements Transferable, ClipboardOwner {

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[0];
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            throw new UnsupportedFlavorException(flavor);
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            // do nothing
        }
    }
}