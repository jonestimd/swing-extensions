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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import io.github.jonestimd.swing.ClientProperty;
import io.github.jonestimd.swing.ComponentFactory;
import io.github.jonestimd.swing.ComponentTreeUtils;
import io.github.jonestimd.swing.action.MnemonicAction;

/**
 * Manages an application's windows. This class creates window and initializes their menu bar with the {@code Windows}
 * and {@code Help} menus.  This class updates the {@code Windows} menu whenever another window is opened or closed.
 * Typically, an instance of this class is registered with a {@link WindowEventPublisher} and menu items for opening
 * new windows are created using {@link FrameAction}.
 * @param <Key> The class defining the window types (typically an {@code enum})
 */
public class FrameManager<Key extends WindowInfo> implements WindowEventListener<Key> {
    protected static final String WINDOWS_MENU_KEY = "menu.windows.mnemonicAndName";
    private static final Predicate<JMenu> IS_WINDOW_MENU = menu -> WINDOWS_MENU_KEY.equals(menu.getClientProperty(ClientProperty.MNEMONIC_AND_NAME_KEY));
    private final ResourceBundle bundle;
    private final Map<Key,? extends Container> singletonPanels;
    private final Map<Key, PanelFactory<? extends ApplicationWindowEvent<Key>>> panelFactories;
    private final Map<Key,StatusFrame> singletonFrames = new HashMap<>();
    private final List<StatusFrame> frames = new ArrayList<>();
    private final Map<StatusFrame,WindowAction> menuActions = new HashMap<>();
    private final FrameListener frameListener = new FrameListener();
    private final Function<Window, Dialog> aboutDialogSupplier;
    private final BiFunction<ResourceBundle, String, StatusFrame> frameFactory;

    /**
     * Create a FrameManager that uses default {@link StatusFrame}s.
     * @param bundle the {@link ResourceBundle} to use for localization of the windows
     * @param singletonPanels a map of the content panels of the singleton windows
     * @param panelFactories a map of the factories for the content panels of the non-singleton windows
     * @param aboutDialogSupplier a factory for creating the {@code About} dialog
     */
    public FrameManager(ResourceBundle bundle, Map<Key,? extends Container> singletonPanels,
            Map<Key,PanelFactory<? extends ApplicationWindowEvent<Key>>> panelFactories,
            Function<Window, Dialog> aboutDialogSupplier) {
        this(bundle, singletonPanels, panelFactories, aboutDialogSupplier, StatusFrame::new);
    }

    /**
     * Create a FrameManager that uses custom {@link StatusFrame}s.
     * @param bundle the {@link ResourceBundle} to use for localization of the windows
     * @param singletonPanels a map of the content panels of the singleton windows
     * @param panelFactories a map of the factories for the content panels of the non-singleton windows
     * @param aboutDialogSupplier a factory for creating the {@code About} dialog
     * @param frameFactory a factory for creating {@link StatusFrame}s for the windows
     */
    public FrameManager(ResourceBundle bundle, Map<Key,? extends Container> singletonPanels,
            Map<Key,PanelFactory<? extends ApplicationWindowEvent<Key>>> panelFactories,
            Function<Window, Dialog> aboutDialogSupplier, BiFunction<ResourceBundle, String, StatusFrame> frameFactory) {
        this.bundle = bundle;
        this.singletonPanels = singletonPanels;
        this.panelFactories = panelFactories;
        this.aboutDialogSupplier = aboutDialogSupplier;
        this.frameFactory = frameFactory;
    }

    /**
     * Display a singleton window or create a new instance of a non-singleton window.
     * @param windowEvent event describing the window
     */
    public void onWindowEvent(ApplicationWindowEvent<Key> windowEvent) {
        Key windowInfo = windowEvent.getWindowInfo();
        if (windowInfo.isSingleton()) showSingletonFrame(windowInfo);
        else createFrame(windowEvent);
    }

    @SuppressWarnings("unchecked")
    private PanelFactory<ApplicationWindowEvent<Key>> getPanelFactory(Key windowInfo) {
        return ((PanelFactory<ApplicationWindowEvent<Key>>) panelFactories.get(windowInfo));
    }

    /**
     * Register a singleton window.
     * @param key the window type
     * @param frame the window
     * @throws IllegalArgumentException if another window has already been registered for the specified {@code key}
     */
    public void addSingletonFrame(Key key, StatusFrame frame) {
        if (! singletonFrames.containsKey(key)) {
            singletonFrames.put(key, frame);
            initializeFrame(frame);
            frame.setContentPane(singletonPanels.get(key));
        }
        else if (singletonFrames.get(key) != frame) {
            throw new IllegalArgumentException("Duplicate JFrame ID: " + key.toString());
        }
    }

    /**
     * Make a singleton window visible or, if it is already visible, bring it to the front.
     * @param key the window type
     * @return the singleton window
     */
    public JFrame showSingletonFrame(Key key) {
        StatusFrame frame = singletonFrames.get(key);
        if (frame == null) {
            frame = frameFactory.apply(bundle, key.getResourcePrefix());
            addSingletonFrame(key, frame);
            frame.setVisible(true);
        }
        else {
            frame.toFront();
        }
        return frame;
    }

    /**
     * Make a non-singleton window visible or, if it is already visible, bring it to the front.
     * @param event the window type
     * @param matcher selects the frame to show
     * @return the window
     */
    public JFrame showFrame(ApplicationWindowEvent<Key> event, Predicate<StatusFrame> matcher) {
        return frames.stream().filter(matcher).findFirst()
                .map(frame -> {
                    frame.toFront();
                    return frame;
                })
                .orElseGet(() -> createFrame(event));
    }

    /**
     * Create and display a non-singleton window.
     * @param event the window event specifying the type of window
     * @return the new window
     */
    protected StatusFrame createFrame(ApplicationWindowEvent<Key> event) {
        Key windowType = event.getWindowInfo();
        StatusFrame frame = frameFactory.apply(bundle, windowType.getResourcePrefix());
        addFrame(frame, getPanelFactory(windowType).createPanel(event));
        frame.setVisible(true);
        return frame;
    }

    /**
     * Add an existing non-singleton window to this manager and initialize its content pane.
     * @param frame the window
     * @param windowType the window type
     * @return the new content pane
     */
    public Container addFrame(StatusFrame frame, Key windowType) {
        Container contentPane = getPanelFactory(windowType).createPanel();
        addFrame(frame, contentPane);
        return contentPane;
    }

    private void addFrame(StatusFrame frame, Container contentPane) {
        frames.add(frame);
        initializeFrame(frame);
        frame.setContentPane(contentPane);
    }

    private JMenuBar createJMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createWindowsMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    private JMenu createHelpMenu() {
        JMenu menu = ComponentFactory.newMenu(bundle, "menu.help.mnemonicAndName");
        menu.add(new MnemonicAction(bundle, "menu.help.about") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = ComponentTreeUtils.findAncestor((Component) e.getSource(), Window.class);
                aboutDialogSupplier.apply(window).setVisible(true);
            }
        });
        return menu;
    }

    private JMenu createWindowsMenu() {
        JMenu menu = ComponentFactory.newMenu(bundle, WINDOWS_MENU_KEY);
        for (WindowAction action : menuActions.values()) {
            menu.add(new JMenuItem(action));
        }
        return menu;
    }

    private void initializeFrame(StatusFrame frame) {
        frame.addWindowListener(frameListener);
        frame.addPropertyChangeListener("title", frameListener);
        frame.setJMenuBar(createJMenuBar());
        addMenuItem(new WindowAction(frame));
    }

    private void addMenuItem(WindowAction action) {
        menuActions.put(action.frame, action);
        addWindowAction(frames, action);
        addWindowAction(singletonFrames.values(), action);
    }

    private void addWindowAction(Collection<StatusFrame> windows, WindowAction action) {
        for (StatusFrame window : windows) {
            getWindowsMenu(window.getJMenuBar()).add(new JMenuItem(action));
        }
    }

    private JMenu getWindowsMenu(JMenuBar menuBar) {
        return ComponentTreeUtils.findComponent(menuBar, JMenu.class, IS_WINDOW_MENU);
    }

    private void removeWindow(StatusFrame frame) {
        WindowAction action = menuActions.remove(frame);
        singletonFrames.values().remove(frame);
        frames.remove(frame);
        removeWindowAction(singletonFrames.values(), action);
        removeWindowAction(frames, action);
        // if it's a singleton this will remove the singleton panel from the frame's hierarchy
        frame.getLayeredPane().remove(frame.getContentPane());
    }

    private void removeWindowAction(Collection<StatusFrame> windows, WindowAction action) {
        for (StatusFrame window : windows) {
            removeMenuItem(getWindowsMenu(window.getJMenuBar()), action);
        }
    }

    private void removeMenuItem(JMenu menu, WindowAction action) {
        for (int i = 0; i < menu.getMenuComponentCount(); i++) {
            Component component = menu.getMenuComponent(i);
            if (getWindowAction(component) == action) {
                menu.remove(component);
            }
        }
    }

    private Action getWindowAction(Component component) {
        return component instanceof JMenuItem ? ((JMenuItem) component).getAction() : null;
    }

    public int getFrameCount() {
        return singletonFrames.size() + frames.size();
    }

    private class WindowAction extends AbstractAction {
        private StatusFrame frame;

        private WindowAction(StatusFrame frame) {
            super(frame.getTitle());
            this.frame = frame;
        }

        public void actionPerformed(ActionEvent e) {
            frame.toFront();
        }
    }

    private class FrameListener extends WindowAdapter implements PropertyChangeListener {
        public void windowActivated(WindowEvent e) {
            menuActions.get(e.getSource()).setEnabled(false);
        }

        public void windowDeactivated(WindowEvent e) {
            if (menuActions.containsKey(e.getSource())) {
                menuActions.get(e.getSource()).setEnabled(true);
            }
        }

        public void windowClosed(WindowEvent e) {
            removeWindow((StatusFrame) e.getSource());
        }

        public void propertyChange(PropertyChangeEvent evt) {
            JFrame frame = (JFrame) evt.getSource();
            menuActions.get(frame).putValue(Action.NAME, frame.getTitle());
        }
    }
}