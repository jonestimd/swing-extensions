package io.github.jonestimd.swing.window;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import io.github.jonestimd.mockito.ArgumentCaptorFactory;
import io.github.jonestimd.swing.ClientProperty;
import io.github.jonestimd.swing.ComponentFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.same;

@RunWith(MockitoJUnitRunner.class)
public class FrameManagerTest {
    private enum Type implements WindowInfo {
        SINGLETON1, SINGLETON2, MULTI;

        public boolean isSingleton() {
            return this != MULTI;
        }
        public String getResourcePrefix() {
            return name();
        }
    }

    private ResourceBundle bundle = ResourceBundle.getBundle(TestResources.class.getName());
    @Mock
    private Function<Type,JPanel> singletonPanelSupplier;
    @Mock
    private ComponentFactory componentFactory;
    @Mock
    private Map<Type,PanelFactory<? extends ApplicationWindowAction<Type>>> panelFactories;
    @Mock
    private PanelFactory<ApplicationWindowAction<Type>> panelFactory;
    @Mock
    private Function<Window, Dialog> aboutDialogSupplier;
    @Mock
    private BiFunction<ResourceBundle, String, StatusFrame> frameFactory;
    @Mock
    private ApplicationWindowAction<Type> action;
    @InjectMocks
    private FrameManager<Type> frameManager;

    @Before
    public void createFrameManager() {
        frameManager = new FrameManager<>(bundle, singletonPanelSupplier, panelFactories, aboutDialogSupplier, frameFactory);
        doReturn(panelFactory).when(panelFactories).get(Type.MULTI);
    }

    @Test
    public void onWindowEventCreatesSingletonWindow() throws Exception {
        final String frameTitle = "Singleton Frame 1";
        StatusFrame frame = mock(StatusFrame.class);
        JMenu windowsMenu = trainGetJMenuBar(frame);
        when(frame.getTitle()).thenReturn(frameTitle);
        when(frameFactory.apply(bundle, Type.SINGLETON1.name())).thenReturn(frame);
        JPanel content = new JPanel();
        when(singletonPanelSupplier.apply(Type.SINGLETON1)).thenReturn(content);
        when(action.getWindowInfo()).thenReturn(Type.SINGLETON1);

        frameManager.onWindowEvent(action);

        assertThat(frameManager.getFrameCount()).isEqualTo(1);
        verifyInitializeFrame(frame);
        verify(frame).setContentPane(content);
        verify(frame).setVisible(true);
        checkJMenuBar(frame, frameTitle, windowsMenu);
    }

    @Test
    public void onWindowEventCreatesMultiFrameWindow() throws Exception {
        final String frameTitle = "Singleton Frame 1";
        final JPanel panel = new JPanel();
        StatusFrame frame = mock(StatusFrame.class);
        JMenu windowsMenu = trainGetJMenuBar(frame);
        when(frame.getTitle()).thenReturn(frameTitle);
        when(frameFactory.apply(bundle, Type.MULTI.name())).thenReturn(frame);
        when(panelFactory.createPanel(any())).thenReturn(panel);
        when(action.getWindowInfo()).thenReturn(Type.MULTI);

        frameManager.onWindowEvent(action);

        assertThat(frameManager.getFrameCount()).isEqualTo(1);
        verifyInitializeFrame(frame);
        verify(frame).setContentPane(panel);
        verify(frame).setVisible(true);
        checkJMenuBar(frame, frameTitle, windowsMenu);
    }

    private void checkJMenuBar(StatusFrame frame, String frameTitle, JMenu windowsMenu) {
        ArgumentCaptor<JMenuBar> captor = ArgumentCaptorFactory.create();
        verify(frame).setJMenuBar(captor.capture());
        assertThat(windowsMenu.getItemCount()).isEqualTo(1);
        assertThat(windowsMenu.getItem(0).getText()).isEqualTo(frameTitle);
        assertThat(captor.getValue().getMenuCount()).isEqualTo(2);
        checkMenu(captor.getValue().getMenu(0), "Windows");
        checkMenu(captor.getValue().getMenu(1), "Help", "About");
    }

    private JMenu trainGetJMenuBar(StatusFrame frame) {
        JMenu windowsMenu = new JMenu("Windows");
        windowsMenu.putClientProperty(ClientProperty.MNEMONIC_AND_NAME_KEY, FrameManager.WINDOWS_MENU_KEY);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(windowsMenu);
        when(frame.getJMenuBar()).thenReturn(menuBar);
        return windowsMenu;
    }

    private void checkMenu(JMenu menu, String text, String... items) {
        assertThat(menu.getText()).isEqualTo(text);
        assertThat(menu.getItemCount()).isEqualTo(items.length);
        for (int i = 0; i < items.length; i++) {
            assertThat(menu.getItem(i).getText()).isEqualToIgnoringCase(items[i]);
        }
    }

    @Test
    public void testAddSingletonFrame() throws Exception {
        StatusFrame frame = spy(new StatusFrame(new TestBundle(), "window"));
        when(singletonPanelSupplier.apply(Type.SINGLETON1)).thenReturn(new JPanel());

        frameManager.addSingletonFrame(Type.SINGLETON1, frame);

        verifyInitializeFrame(frame);
        checkWindowsMenu(frame.getJMenuBar().getMenu(0), "frame1");
        assertThat(frameManager.getFrameCount()).isEqualTo(1);
    }

    private void verifyInitializeFrame(StatusFrame frame) {
        verify(frame).addWindowListener(isA(WindowListener.class));
        verify(frame).addPropertyChangeListener(eq("title"), isA(PropertyChangeListener.class));
        verify(frame).setJMenuBar(isA(JMenuBar.class));
    }

    private void checkWindowsMenu(JMenu windowsMenu, String title) {
        assertThat(windowsMenu.getMenuComponentCount()).isEqualTo(1);
        assertThat(((JMenuItem) windowsMenu.getMenuComponent(0)).getText()).isEqualTo(title);
    }

    @Test
    public void testAddSingletonFrameTwice() throws Exception {
        StatusFrame frame = spy(new StatusFrame(new TestBundle(), "window"));
        JPanel content = new JPanel();
        when(singletonPanelSupplier.apply(Type.SINGLETON1)).thenReturn(content);

        frameManager.addSingletonFrame(Type.SINGLETON1, frame);
        frameManager.addSingletonFrame(Type.SINGLETON1, frame);

        verifyInitializeFrame(frame);
        JMenu windowsMenu = frame.getJMenuBar().getMenu(0);
        assertThat(windowsMenu.getItemCount()).isEqualTo(1);
        checkWindowsMenu(windowsMenu, "frame1");
        assertThat(frameManager.getFrameCount()).isEqualTo(1);
    }

    @Test
    public void testAddSingletonFrameWithExistingId() throws Exception {
        StatusFrame frame1 = spy(new StatusFrame(new TestBundle(), "window"));
        StatusFrame frame2 = spy(new StatusFrame(new TestBundle(), "window"));
        when(singletonPanelSupplier.apply(Type.SINGLETON1)).thenReturn(new JPanel());

        frameManager.addSingletonFrame(Type.SINGLETON1, frame1);
        try {
            frameManager.addSingletonFrame(Type.SINGLETON1, frame2);
            Assert.fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Duplicate JFrame ID: SINGLETON1");
        }

        verifyInitializeFrame(frame1);
        checkWindowsMenu(frame1.getJMenuBar().getMenu(0), "frame1");
        assertThat(frameManager.getFrameCount()).isEqualTo(1);
    }

    @Test
    public void testGetSingletonFrameWithNewId() throws Exception {
        StatusFrame frame = spy(new StatusFrame(new TestBundle(), "window"));
        doNothing().when(frame).setVisible(anyBoolean());
        when(frameFactory.apply(bundle, "SINGLETON1")).thenReturn(frame);
        JPanel content = new JPanel();
        when(singletonPanelSupplier.apply(Type.SINGLETON1)).thenReturn(content);

        assertThat(frameManager.showSingletonFrame(Type.SINGLETON1)).isSameAs(frame);
        assertThat(frameManager.showSingletonFrame(Type.SINGLETON1)).isSameAs(frame);

        verify(singletonPanelSupplier, times(1)).apply(Type.SINGLETON1);
        verify(frame).setContentPane(same(content));
        verify(frame).setVisible(true);
        verify(frame).toFront();
        checkWindowsMenu(frame.getJMenuBar().getMenu(0), "frame1");
    }

    @Test
    public void testShowFrameShowsMatchingFrame() throws Exception {
        StatusFrame frame = spy(new StatusFrame(new TestBundle(), "window"));
        JPanel contentPane = new JPanel();
        when(panelFactory.createPanel()).thenReturn(contentPane);
        frameManager.addFrame(frame, Type.MULTI);
        when(action.getWindowInfo()).thenReturn(Type.MULTI);
        when(action.matches(frame)).thenReturn(true);

        assertThat(frameManager.showFrame(action)).isSameAs(frame);

        assertThat(frameManager.getFrameCount()).isEqualTo(1);
        verifyZeroInteractions(frameFactory);
        verify(panelFactory).createPanel();
        verifyNoMoreInteractions(panelFactory);
    }

    @Test
    public void testShowFrameCreatesFrame() throws Exception {
        StatusFrame frame = spy(new StatusFrame(new TestBundle(), "window"));
        when(frameFactory.apply(bundle, "MULTI")).thenReturn(frame);
        JPanel contentPane = new JPanel();
        when(panelFactory.createPanel(action)).thenReturn(contentPane);
        when(action.getWindowInfo()).thenReturn(Type.MULTI);
        when(action.matches(any(StatusFrame.class))).thenReturn(false);

        assertThat(frameManager.showFrame(action)).isSameAs(frame);

        assertThat(frameManager.getFrameCount()).isEqualTo(1);
        verify(frameFactory).apply(bundle, "MULTI");
        verify(panelFactory).createPanel(action);
        frame.dispose();
    }

    @Test
    public void testFrameClosed() throws Exception {
        StatusFrame frame1 = new StatusFrame(new TestBundle(), "window");
        StatusFrame frame2 = new StatusFrame(new TestBundle(), "window");
        JPanel content1 = new JPanel();
        JPanel content2 = new JPanel();
        when(singletonPanelSupplier.apply(Type.SINGLETON1)).thenReturn(content1);
        when(singletonPanelSupplier.apply(Type.SINGLETON2)).thenReturn(content2);

        frameManager.addSingletonFrame(Type.SINGLETON1, frame1);
        frameManager.addSingletonFrame(Type.SINGLETON2, frame2);

        frame1.getWindowListeners()[0].windowClosed(new WindowEvent(frame1, WindowEvent.WINDOW_CLOSED));

        assertThat(frameManager.getFrameCount()).isEqualTo(1);
        assertThat(frame2.getJMenuBar().getMenu(0).getMenuComponentCount()).isEqualTo(1);
        assertThat(content1.getParent()).isNull();
    }

    public static class TestBundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new String[][] {
                { "window.title", "frame1" },
            };
        }
    }

    public static class TestResources extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[][] {
                { "menu.windows.mnemonicAndName", "WWindows" },
                { "menu.help.mnemonicAndName", "HHelp" },
                { "menu.help.about.mnemonicAndName", "AAbout" },
                { "SINGLETON1.title", "Singleton1" },
            };
        }
    }
}