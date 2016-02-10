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

import io.github.jonestimd.swing.ComponentFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

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
    private ComponentFactory componentFactory;
    @Mock
    private Map<Type,JPanel> singletonPanels;
    @Mock
    private Map<Type,PanelFactory<? extends ApplicationWindowEvent<Type>>> panelFactories;
    @Mock
    private Function<Window, Dialog> aboutDialogSupplier;
    @Mock
    private BiFunction<ResourceBundle, String, StatusFrame> frameFactory;
    @InjectMocks
    private FrameManager<Type> frameManager;

    @Before
    public void createFrameManager() {
        frameManager = new FrameManager<>(bundle, singletonPanels, panelFactories, aboutDialogSupplier, frameFactory);
    }

    @Test
    public void testAddSingletonFrame() throws Exception {
        StatusFrame frame = spy(new StatusFrame(new TestBundle(), "window"));

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
        JPanel contentPane = new JPanel();
        when(singletonPanels.get(Type.SINGLETON1)).thenReturn(contentPane);

        assertThat(frameManager.showSingletonFrame(Type.SINGLETON1)).isSameAs(frame);
        assertThat(frameManager.showSingletonFrame(Type.SINGLETON1)).isSameAs(frame);

        verify(frame).setContentPane(contentPane);
        verify(frame).setVisible(true);
        verify(frame).toFront();
        checkWindowsMenu(frame.getJMenuBar().getMenu(0), "frame1");
    }

    @Test
    public void testAddFrame() throws Exception {
        StatusFrame frame = spy(new StatusFrame(new TestBundle(), "window"));
        when(frameFactory.apply(bundle, "MULTI")).thenReturn(frame);
        JPanel contentPane = new JPanel();

        assertThat(frameManager.createFrame(Type.MULTI, contentPane)).isSameAs(frame);

        verifyInitializeFrame(frame);
        verify(frame).setContentPane(contentPane);
        checkWindowsMenu(frame.getJMenuBar().getMenu(0), "frame1");
        assertThat(frameManager.getFrameCount()).isEqualTo(1);
    }

    @Test
    public void testFrameClosed() throws Exception {
        StatusFrame frame1 = new StatusFrame(new TestBundle(), "window");
        StatusFrame frame2 = new StatusFrame(new TestBundle(), "window");

        frameManager.addSingletonFrame(Type.SINGLETON1, frame1);
        frameManager.addSingletonFrame(Type.SINGLETON2, frame2);

        frame1.getWindowListeners()[0].windowClosed(new WindowEvent(frame1, WindowEvent.WINDOW_CLOSED));

        assertThat(frameManager.getFrameCount()).isEqualTo(1);
        assertThat(frame2.getJMenuBar().getMenu(0).getMenuComponentCount()).isEqualTo(1);
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
                { "menu.windows.mnemonicAndName", "Windows" },
                { "menu.help.mnemonicAndName", "HHelp" },
                { "menu.help.about.mnemonicAndName", "AAbout" },
                { "SINGLETON1.title", "Singleton1" },
            };
        }
    }
}