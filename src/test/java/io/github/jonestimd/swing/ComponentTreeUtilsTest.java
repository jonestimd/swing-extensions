package io.github.jonestimd.swing;

import java.awt.Container;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Test;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

public class ComponentTreeUtilsTest {
    @Test @SuppressWarnings("unchecked")
    public void testVisitComponentTree() throws Exception {
        JPanel root = new JPanel();
        JPanel child1 = new JPanel();
        Container child2 = new Container();
        root.add(child1);
        root.add(child2);
        JPanel grandChild1 = new JPanel();
        JPanel grandChild2 = new JPanel();
        child1.add(grandChild1);
        child2.add(grandChild2);
        ComponentVisitor<JComponent> visitor = mock(ComponentVisitor.class);

        ComponentTreeUtils.visitComponentTree(root, JComponent.class, visitor);

        verify(visitor).visit(root);
        verify(visitor).visit(child1);
        verify(visitor).visit(grandChild1);
        verify(visitor).visit(grandChild2);
    }

    @Test
    public void testFindAncestor() throws Exception {
        JFrame frame = new JFrame();
        frame.setContentPane(new JPanel());

        assertThat(ComponentTreeUtils.findAncestor(frame.getContentPane(), Window.class)).isSameAs(frame);
        assertThat(ComponentTreeUtils.findAncestor(frame, Window.class)).isSameAs(frame);
    }
}