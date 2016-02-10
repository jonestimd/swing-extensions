package io.github.jonestimd.mockito;

import org.mockito.ArgumentCaptor;

@SuppressWarnings("deprecation")
public class ArgumentCaptorFactory {
    public static <T> ArgumentCaptor<T> create() {
        return new ArgumentCaptor<>();
    }
}
