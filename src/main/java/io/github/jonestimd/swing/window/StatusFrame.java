// Copyright (c) 2016 Timothy D. Jones
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
package io.github.jonestimd.swing.window;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.SettingsPersister;
import io.github.jonestimd.swing.StatusIndicator;
import io.github.jonestimd.swing.UnsavedChangesIndicator;
import io.github.jonestimd.swing.component.AlphaPanel;

/**
 * This class extends {@link JFrame} to disable user input and provide progress messages during long running processes.
 * It also uses {@link SettingsPersister} to store state when the window is closed.
 */
public class StatusFrame extends JFrame implements StatusIndicator, UnsavedChangesIndicator {
    public static final float GLASS_PANE_ALPHA = 0.5f;
    public static final String SMALL_ICON_RESOURCE_KEY = ".smallIconImage";
    public static final String LARGE_ICON_RESOURCE_KEY = ".largeIconImage";
    private static final String STATE_SUFFIX = ".state";
    private static final String HEIGHT_SUFFIX = ".height";
    private static final String WIDTH_SUFFIX = ".width";
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    protected final Logger logger = Logger.getLogger(StatusFrame.class.getName());
    private final ResourceBundle bundle;
    private JLabel statusMessageLabel = new JLabel();
    private String resourcePrefix;
    private String baseTitle;
    private Component lastFocusOwner;

    public StatusFrame(ResourceBundle bundle, String resourcePrefix) {
        super(bundle.getString(resourcePrefix + ".title"));
        this.bundle = bundle;
        this.baseTitle = getTitle();
        this.resourcePrefix = resourcePrefix;
        setIcons(bundle, SMALL_ICON_RESOURCE_KEY, LARGE_ICON_RESOURCE_KEY);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        createGlassPane();
        statusMessageLabel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(5, 5, 5, 5)));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", this::focusChanged);
    }

    private void focusChanged(PropertyChangeEvent event) {
        Component newValue = (Component) event.getNewValue();
        if (ComponentTreeUtils.findAncestor(newValue, this.getContentPane()::equals).isPresent()) {
            lastFocusOwner = newValue;
        }
    }

    private void setIcons(ResourceBundle bundle, String ... resourceKeys) {
        List<Image> icons = new ArrayList<>();
        for (String resourceKey : resourceKeys) {
            if (bundle.containsKey(resourcePrefix + resourceKey)) {
                icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource(bundle.getString(resourcePrefix + resourceKey))));
            }
        }
        if (! icons.isEmpty()) {
            setIconImages(icons);
        }
    }

    private void createGlassPane() {
        AlphaPanel glassPane = new AlphaPanel(new GridBagLayout(), GLASS_PANE_ALPHA) {
            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                super.processKeyBinding(ks, e, condition, pressed);
                // block keyboard events when glass pane is visible
                return true;
            }
        };
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        glassPane.add(statusMessageLabel, gbc);
        // block mouse events when glass pane is visible
        MouseAdapter mouseAdapter = new MouseAdapter() {};
        glassPane.addMouseListener(mouseAdapter);
        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setGlassPane(glassPane);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        baseTitle = title;
    }

    @Override
    public void setExtendedState(int state) {
        if ((state & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
            System.setProperty(getPropertyName(WIDTH_SUFFIX), Integer.toString(getWidth()));
            System.setProperty(getPropertyName(HEIGHT_SUFFIX), Integer.toString(getHeight()));
        }
        super.setExtendedState(state);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            pack();
            setLocationByPlatform(true);
            restoreSize();
        }
        super.setVisible(visible);
    }

    private void restoreSize() {
        int width = getInt(getPropertyName(WIDTH_SUFFIX), DEFAULT_WIDTH);
        int height = getInt(getPropertyName(HEIGHT_SUFFIX), DEFAULT_HEIGHT);
        setSize(new Dimension(width, height));
        int state = Integer.getInteger(getPropertyName(STATE_SUFFIX), 0);
        if ((state & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
            setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
        }
    }

    private int getInt(String property, int defaultValue) {
        try {
            Integer value = Integer.getInteger(property, -1);
            return value < 0 ? Integer.parseInt(bundle.getString(property)) : value;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private String getPropertyName(String suffix) {
        return resourcePrefix + suffix;
    }

    /**
     * Override to save window state via {@link SettingsPersister}.
     */
    @Override
    public void dispose() {
        System.setProperty(getPropertyName(STATE_SUFFIX), Integer.toString(getExtendedState()));
        if ((getExtendedState() & MAXIMIZED_BOTH) != MAXIMIZED_BOTH) {
            System.setProperty(getPropertyName(WIDTH_SUFFIX), Integer.toString(getWidth()));
            System.setProperty(getPropertyName(HEIGHT_SUFFIX), Integer.toString(getHeight()));
        }
        ComponentTreeUtils.visitComponentTree(getContentPane(), JComponent.class, SettingsPersister::saveSettings);
        super.dispose();
    }

    /**
     * Update the progress message.
     */
    @Override
    public void setStatusMessage(String message) {
        statusMessageLabel.setText(message);
    }

    /**
     * Disable user input and display a progress message.
     */
    @Override
    public void disableUI(String message) {
        logger.log(Level.FINE, "disableUI {0}", message);
        setStatusMessage(message);
        getGlassPane().setVisible(true);
        getGlassPane().requestFocus();
    }

    /**
     * Enable user input and hide the progress message.
     */
    @Override
    public void enableUI() {
        logger.fine("enableUI");
        getGlassPane().setVisible(false);
        if (lastFocusOwner != null) {
            lastFocusOwner.requestFocusInWindow();
        }
    }

    /**
     * Update the window title to indicate unsaved changes.
     * @param unsavedChanges true to add the {@code unsaved} indicator, false to remove it
     */
    @Override
    public void setUnsavedChanges(boolean unsavedChanges) {
        super.setTitle(unsavedChanges ? baseTitle + " *": baseTitle);
    }
}