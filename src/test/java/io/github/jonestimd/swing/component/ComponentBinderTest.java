package io.github.jonestimd.swing.component;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import io.github.jonestimd.beans.ObservableBean;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.*;

public class ComponentBinderTest {
    @Test
    public void bindBeanToTextField() throws Exception {
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
    public void rebindBeanToTextField() throws Exception {
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

    @Test
    @SuppressWarnings("unchecked")
    public void onChangeCallsHandler() throws Exception {
        Runnable handler = mock(Runnable.class);

        JTextField field = ComponentBinder.onChange(new JTextField(), handler);
        field.setText("text");

        verify(handler).run();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bindTextFieldToConsumer() throws Exception {
        Consumer<String> consumer = mock(Consumer.class);

        JTextField field = ComponentBinder.bind(new JTextField(), consumer);
        field.setText("text");

        verify(consumer).accept("text");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bindTextFieldToConsumerWithParser() throws Exception {
        Function<String, String> parser = mock(Function.class);
        Consumer<String> consumer = mock(Consumer.class);
        when(parser.apply(anyString())).thenAnswer(invocation -> invocation.getArguments()[0].toString().toUpperCase());

        JTextField field = ComponentBinder.bind(new JTextField(), parser, consumer);
        field.setText("text");

        verify(parser).apply("text");
        verify(consumer).accept("TEXT");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void bindPasswordFieldToConsumer() throws Exception {
        Consumer<String> consumer = mock(Consumer.class);

        JPasswordField field = ComponentBinder.bind(new JPasswordField(), consumer);
        field.setText("text");

        verify(consumer).accept("text");
    }
}
