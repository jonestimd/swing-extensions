package io.github.jonestimd.swing.component;

import org.junit.Test;

import static org.junit.Assert.*;

public class BeanListModelTest {
    @Test
    public void addElementAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<String>();

        model.addElement(null);

        assertEquals(1, model.getSize());
        assertNull(model.getElementAt(0));
    }

    @Test
    public void insertElementAtAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<String>();

        model.insertElementAt(null, 0);

        assertEquals(1, model.getSize());
        assertNull(model.getElementAt(0));
    }

    @Test
    public void removeElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<String>();
        model.addElement("one");
        model.addElement("two");

        model.removeElement("one");

        assertEquals(1, model.getSize());
        assertEquals("two", model.getElementAt(0));
    }

    @Test
    public void removeElementAt() throws Exception {
        BeanListModel<String> model = new BeanListModel<String>();
        model.addElement("one");
        model.addElement("two");

        model.removeElementAt(1);

        assertEquals(1, model.getSize());
        assertEquals("one", model.getElementAt(0));
    }

    @Test
    public void setSelectedElementAllowsNull() throws Exception {
        BeanListModel<String> model = new BeanListModel<String>();

        model.setSelectedItem(null);

        assertNull(model.getSelectedItem());
    }

    @Test
    public void setSelectedElement() throws Exception {
        BeanListModel<String> model = new BeanListModel<String>();

        model.setSelectedItem("one");

        assertEquals("one", model.getSelectedItem());
    }
}