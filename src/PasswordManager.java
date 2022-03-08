import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.util.logging.Level;
import java.util.logging.Logger;

class PasswordManager {

    private static final Logger LOG = Logger.getLogger(PasswordManager.class.getName());
    private static final String METAL_LOOK_AND_FEEL = "javax.swing.plaf.metal.MetalLookAndFeel";

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tF %1$tT] [%4$-7s] %5$s %n");
    }

    public static void main(final String[] args) {
        try {
            String lookAndFeel;
            if (Configuration.getInstance().is("system.look.and.feel.enabled", true)) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else {
                lookAndFeel = METAL_LOOK_AND_FEEL;
            }

            if (METAL_LOOK_AND_FEEL.equals(lookAndFeel)) {
                MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme() {
                    private final ColorUIResource primary1 = new ColorUIResource(0x4d6781);
                    private final ColorUIResource primary2 = new ColorUIResource(0x7a96b0);
                    private final ColorUIResource primary3 = new ColorUIResource(0xc8d4e2);
                    private final ColorUIResource secondary1 = new ColorUIResource(0x000000);
                    private final ColorUIResource secondary2 = new ColorUIResource(0xaaaaaa);
                    private final ColorUIResource secondary3 = new ColorUIResource(0xdfdfdf);

                    @Override
                    protected ColorUIResource getPrimary1() {
                        return this.primary1;
                    }

                    @Override
                    protected ColorUIResource getPrimary2() {
                        return this.primary2;
                    }

                    @Override
                    protected ColorUIResource getPrimary3() {
                        return this.primary3;
                    }

                    @Override
                    protected ColorUIResource getSecondary1() {
                        return this.secondary1;
                    }

                    @Override
                    protected ColorUIResource getSecondary2() {
                        return this.secondary2;
                    }

                    @Override
                    protected ColorUIResource getSecondary3() {
                        return this.secondary3;
                    }
                });

                UIManager.put("swing.boldMetal", Boolean.FALSE);
            }

            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            LOG.log(Level.CONFIG, "Could not set look and feel for the application", e);
        }

        SwingUtilities.invokeLater(() -> PasswordManagerFrame.getInstance((args.length > 0) ? args[0] : null));
    }
}

/**
 * Simple callback method interface.
 */
interface Callback {

    /**
     * Callback method.
     *
     * @param result the result of the callback
     */
    void call(boolean result);
}