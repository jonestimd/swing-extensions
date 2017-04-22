package io.github.jonestimd.swing;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ComponentTreeUtilsTest {
    @Mock
    private ComponentVisitor<Component> visitor;
    private JMenu menu = new JMenu();
    private JMenuItem menuItem = new JMenuItem();
    private JPanel root = setName(new JPanel(), "root");
    private JPanel child1 = setName(new JPanel(), "child1");
    private Container child2 = setName(new Container(), "child2");
    private Canvas child3 = new Canvas();
    private JPanel grandChild1 = setName(new JPanel(), "grandChild1");
    private JPanel grandChild2 = setName(new JPanel(), "grandChild2");

    private <C extends Component> C setName(C component, String name) {
        component.setName(name);
        return component;
    }

    @Before
    public void createTree() {
        root.add(menu);
        root.add(child1);
        root.add(child2);
        root.add(child3);
        child1.add(grandChild1);
        child2.add(grandChild2);
        menu.add(menuItem);
    }

    @Test
    public void visitsAllComponentsInTree() throws Exception {
        ComponentTreeUtils.visitComponentTree(root, visitor);

        verify(visitor).visit(root);
        verify(visitor).visit(menu);
        verify(visitor).visit(menuItem);
        verify(visitor).visit(child1);
        verify(visitor).visit(child2);
        verify(visitor).visit(child3);
        verify(visitor).visit(grandChild1);
        verify(visitor).visit(grandChild2);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void visitsComponentsOfSpecifiedTypeInTree() throws Exception {
        ComponentTreeUtils.visitComponentTree(root, JComponent.class, visitor);

        verify(visitor).visit(root);
        verify(visitor).visit(menu);
        verify(visitor).visit(menuItem);
        verify(visitor).visit(child1);
        verify(visitor).visit(grandChild1);
        verify(visitor).visit(grandChild2);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void visitsChildrenOfComponentsMatchingPredicate() throws Exception {
        ComponentTreeUtils.visitComponentTree(root, Component.class, visitor, JPanel.class::isInstance);

        verify(visitor).visit(root);
        verify(visitor).visit(menu);
        verify(visitor).visit(child1);
        verify(visitor).visit(child2);
        verify(visitor).visit(child3);
        verify(visitor).visit(grandChild1);
        verifyNoMoreInteractions(visitor);
    }

    @Test
    public void findComponentReturnsFirstMatch() throws Exception {
        assertThat(ComponentTreeUtils.findComponent(root, JPanel.class)).isSameAs(child1);
        assertThat(ComponentTreeUtils.findComponent(root, Container.class, namePredicate("child2"))).isSameAs(child2);
        assertThat(ComponentTreeUtils.findComponent(root, Container.class, namePredicate("grandChild1"))).isSameAs(grandChild1);
        assertThat(ComponentTreeUtils.findComponent(root, JTextField.class, namePredicate("grandChild1"))).isNull();
        assertThat(ComponentTreeUtils.findComponent(root, Container.class, namePredicate("not here"))).isNull();
    }

    private <C extends Component> Predicate<C> namePredicate(String name) {
        return c -> name.equals(c.getName());
    }

    @Test
    public void testFindAncestor() throws Exception {
        JFrame frame = new JFrame();
        frame.setContentPane(new JPanel());

        assertThat(ComponentTreeUtils.findAncestor(frame.getContentPane(), Window.class)).isSameAs(frame);
        assertThat(ComponentTreeUtils.findAncestor(frame, Window.class)).isSameAs(frame);
        assertThat(ComponentTreeUtils.findAncestor(frame, JDialog.class)).isNull();
    }

    @Test
    public void isAncestor() throws Exception {
        assertThat(ComponentTreeUtils.isAncestor(grandChild1, null)).isFalse();
        assertThat(ComponentTreeUtils.isAncestor(grandChild1, root)).isTrue();
        assertThat(ComponentTreeUtils.isAncestor(grandChild1, child2)).isFalse();
    }

    @Test
    public void getParentUsesJPopupMenuInvoker() throws Exception {
        JPopupMenu popupMenu = mock(JPopupMenu.class);
        when(popupMenu.getParent()).thenReturn(null);
        when(popupMenu.getInvoker()).thenReturn(root);

        assertThat(ComponentTreeUtils.getParent(popupMenu)).isSameAs(root);
    }
}