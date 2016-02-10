package io.github.jonestimd.swing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentChangeHandlerTest {
    @Mock
    private Runnable handler;
    @InjectMocks
    private DocumentChangeHandler changeHandler;

    @Test
    public void insertUpdateCallsHandler() throws Exception {
        changeHandler.insertUpdate(null);

        verify(handler).run();
    }

    @Test
    public void changedUpdateCallsHandler() throws Exception {
        changeHandler.changedUpdate(null);

        verify(handler).run();
    }

    @Test
    public void removeUpdateCallsHandler() throws Exception {
        changeHandler.removeUpdate(null);

        verify(handler).run();
    }
}