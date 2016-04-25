package io.github.jonestimd.swing.component;

import java.util.Iterator;

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanListModelTest {
    @Test
    public void addElementAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();

        model.addElement(null);

        assertEquals(1, model.getSize());
        assertNull(model.getElementAt(0));
    }

    @Test
    public void insertElementAtAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();

        model.insertElementAt(null, 0);

        assertEquals(1, model.getSize());
        assertNull(model.getElementAt(0));
    }

    @Test
    public void removeElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addElement("one");
        model.addElement("two");

        model.removeElement("one");

        assertEquals(1, model.getSize());
        assertEquals("two", model.getElementAt(0));
    }

    @Test
    public void removeElementAt() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();
        model.addElement("one");
        model.addElement("two");

        model.removeElementAt(1);

        assertEquals(1, model.getSize());
        assertEquals("one", model.getElementAt(0));
    }

    @Test
    public void setSelectedElementAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();

        model.setSelectedItem(null);

        assertNull(model.getSelectedItem());
    }

    @Test
    public void setSelectedElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<>();

        model.setSelectedItem("one");

        assertEquals("one", model.getSelectedItem());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void iteratorIsReadOnly() throws Exception {
        BeanListModel<String> model = new BeanListModel<>(Lists.newArrayList("one", "two"));

        Iterator<String> iterator = model.iterator();
        iterator.remove();
    }
}