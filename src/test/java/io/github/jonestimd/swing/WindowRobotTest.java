// The MIT License (MIT)
//
// Copyright (c) 2017 Timothy D. Jones
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
package io.github.jonestimd.swing;

import java.awt.Window;
import java.util.function.Supplier;

import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.junit.After;
import org.junit.Before;

public abstract class WindowRobotTest<T extends Window & RootPaneContainer> {
    protected T window;
    protected Robot robot;
    private final Supplier<T> newWindow;

    protected WindowRobotTest(Supplier<T> newWindow) {
        this.newWindow = newWindow;
    }

    @Before
    public void createRobot() throws Exception {
        robot = BasicRobot.robotWithNewAwtHierarchy();
    }

    @After
    public void cleanUp() throws Exception {
        robot.cleanUp();
        if (window != null && window.isVisible()) SwingUtilities.invokeAndWait(window::dispose);
    }

    protected void showWindow() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            window = newWindow.get();
            window.setContentPane(createContentPane());
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }

    protected abstract JPanel createContentPane();
}
