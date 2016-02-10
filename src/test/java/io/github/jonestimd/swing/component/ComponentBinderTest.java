package io.github.jonestimd.swing.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.Format;

import javax.swing.JTextField;

import io.github.jonestimd.beans.ObservableBean;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ComponentBinderTest {
    @Test
    public void bind() throws Exception {
        ObservableBean bean = mock(ObservableBean.class);
        JTextField field = new JTextField();
        Format format = new DecimalFormat("#0.00");

        ComponentBinder.bind(bean, "property", 5, field, format);

        ArgumentCaptor<PropertyChangeListener> listenerCaptor = ArgumentCaptor.forClass(PropertyChangeListener.class);
        verify(bean).addPropertyChangeListener(eq("property"), listenerCaptor.capture());
        assertThat(field.getText()).isEqualTo("5.00");

        listenerCaptor.getValue().propertyChange(new PropertyChangeEvent(bean, "property", null, 10));
        assertThat(field.getText()).isEqualTo("10.00");
    }

    @Test
    public void rebind() throws Exception {
        ObservableBean bean1 = mock(ObservableBean.class);
        ObservableBean bean2 = mock(ObservableBean.class);
        JTextField field = new JTextField();
        Format format = new DecimalFormat("#0.00");
        ArgumentCaptor<PropertyChangeListener> listenerCaptor = ArgumentCaptor.forClass(PropertyChangeListener.class);
        ComponentBinder.bind(bean1, "property", 1, field, format);
        verify(bean1).addPropertyChangeListener(eq("property"), listenerCaptor.capture());
        PropertyChangeListener listener1 = listenerCaptor.getValue();

        ComponentBinder.bind(bean2, "property", 2, field, format);

        verify(bean1).removePropertyChangeListener("property", listener1);
        verify(bean2).addPropertyChangeListener(eq("property"), listenerCaptor.capture());
        assertThat(field.getText()).isEqualTo("2.00");

        listenerCaptor.getValue().propertyChange(new PropertyChangeEvent(bean1, "property", null, 10));
        assertThat(field.getText()).isEqualTo("10.00");
    }
}
