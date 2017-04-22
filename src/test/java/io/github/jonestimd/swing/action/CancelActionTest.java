package io.github.jonestimd.swing.action;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CancelActionTest {
    private JDialog dialog = new JDialog();

    @Test
    public void installOnDialogSetsDefaultCloseOperation() throws Exception {
        CancelAction.install(dialog);

        assertThat(dialog.getDefaultCloseOperation()).isEqualTo(WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    @Test
    public void installOnDialogSetsEscapeAction() throws Exception {
        CancelAction action = CancelAction.install(dialog);

        InputMap inputMap = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Object key = inputMap.get(KeyStroke.getKeyStroke("ESCAPE"));
        assertThat(dialog.getRootPane().getActionMap().get(key)).isSameAs(action);
    }

    @Test
    public void cancelledIsInitiallyFalse() throws Exception {
        CancelAction action = CancelAction.install(dialog);

        assertThat(action.isCancelled()).isFalse();
    }

    protected void fireWindowClosing(JDialog window) {
        WindowEvent event = new WindowEvent(window, WindowEvent.WINDOW_CLOSING);
        for (WindowListener listener : window.getWindowListeners()) {
            listener.windowClosing(event);
        }
    }

    @Test
    public void closingTheWindowFiresActionPerformed() throws Exception {
        JDialog spy = spy(dialog);
        CancelAction action = CancelAction.install(spy);

        fireWindowClosing(spy);

        assertThat(action.isCancelled()).isTrue();
        verify(spy).dispose();
    }

    @Test
    public void resetClearsCancelled() throws Exception {
        CancelAction action = CancelAction.install(dialog);
        fireWindowClosing(dialog);

        action.reset();

        assertThat(action.isCancelled()).isFalse();
    }

    @Test
    public void windowIsNotClosedIfConfirmCloseReturnsFalse() throws Exception {
        JDialog spy = spy(dialog);
        CancelAction action = CancelAction.install(spy);
        ConfirmClose confirmClose = mock(ConfirmClose.class);
        when(confirmClose.confirmClose()).thenReturn(false);
        action.setConfirmClose(confirmClose);

        fireWindowClosing(spy);

        assertThat(action.isCancelled()).isFalse();
        verify(confirmClose).confirmClose();
        verify(spy, never()).dispose();
    }

    @Test
    public void windowIsClosedIfConfirmCloseReturnsTrue() throws Exception {
        JDialog spy = spy(dialog);
        CancelAction action = CancelAction.install(spy);
        ConfirmClose confirmClose = mock(ConfirmClose.class);
        when(confirmClose.confirmClose()).thenReturn(true);
        action.setConfirmClose(confirmClose);

        fireWindowClosing(spy);

        assertThat(action.isCancelled()).isTrue();
        verify(confirmClose).confirmClose();
        verify(spy).dispose();
    }
}