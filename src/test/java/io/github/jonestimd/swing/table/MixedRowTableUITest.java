package io.github.jonestimd.swing.table;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import io.github.jonestimd.swing.table.model.BufferedHeaderDetailTableModel;
import io.github.jonestimd.swing.table.model.ColumnAdapter;
import io.github.jonestimd.swing.table.model.FunctionColumnAdapter;
import io.github.jonestimd.swing.table.model.SingleTypeDetailAdapter;

public class MixedRowTableUITest {
    private ResourceBundle bundle = ResourceBundle.getBundle(Bundle.class.getName());
    private ColumnAdapter<HeaderBean, String> headerNameAdapter =
        new FunctionColumnAdapter<>(bundle, "header.", "name", String.class, HeaderBean::getName, HeaderBean::setName);
    private ColumnAdapter<HeaderBean, String> headerDescriptionAdapter =
        new FunctionColumnAdapter<>(bundle, "header.", "description", String.class, HeaderBean::getDescription, HeaderBean::setDescription);
    private ColumnAdapter<DetailBean, String> detailNameAdapter =
        new FunctionColumnAdapter<>(bundle, "detail.", "name", String.class, DetailBean::getName, DetailBean::setName);
    private ColumnAdapter<DetailBean, Double> detailNumberAdapter =
        new FunctionColumnAdapter<>(bundle, "detail.", "number", Double.class, DetailBean::getNumber, DetailBean::setNumber);
    private JFrame frame = new JFrame("Mixed Row Table Test");
    @SuppressWarnings("unchecked")
    private List<ColumnAdapter<HeaderBean, ?>> headerAdapters = Arrays.<ColumnAdapter<HeaderBean, ?>>asList(
            headerNameAdapter, headerDescriptionAdapter);
    private List<ColumnAdapter<?, ?>> detailAdapters = Arrays.<ColumnAdapter<?, ?>>asList(
            detailNameAdapter, detailNumberAdapter);
    private BufferedHeaderDetailTableModel<HeaderBean> tableModel = newModel();
    private MixedRowTable<HeaderBean, BufferedHeaderDetailTableModel<HeaderBean>> table = new MixedRowTable<>(tableModel);

    private MixedRowTableUITest() {
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
        tableModel.addBean(new HeaderBean("header 1", new DetailBean("detail 1a", 1.1d), new DetailBean("detail 1b", 1.2d)));
        tableModel.addBean(new HeaderBean("header 2", new DetailBean("detail 2a", 2.1d), new DetailBean("detail 2b", 2.2d)));
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new MixedRowTableUITest();
    }

    public BufferedHeaderDetailTableModel<HeaderBean> newModel() {
        return new BufferedHeaderDetailTableModel<>(new TestDetailAdapter(),
            headerAdapters, Collections.singletonList(detailAdapters));
    }

    public static class DetailBean {
        private String name;
        private double number;

        private DetailBean(String name, double number) {
            this.name = name;
            this.number = number;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getNumber() {
            return number;
        }

        public void setNumber(double number) {
            this.number = number;
        }
    }

    public static class HeaderBean {
        private String name;
        private String description;
        private List<DetailBean> details;

        private HeaderBean(String name, DetailBean ... details) {
            this.name = name;
            this.description = name + " description";
            this.details = Arrays.asList(details);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private class TestDetailAdapter extends SingleTypeDetailAdapter<HeaderBean> {
        public List<?> getDetails(HeaderBean bean, int subRowTypeIndex) {
            return bean.details;
        }

        @Override
        public  int appendDetail(HeaderBean bean) {
            throw new UnsupportedOperationException();
        }
    }

    public static class Bundle extends ListResourceBundle {
        protected Object[][] getContents() {
            return new String[][] {
                { "header.name", "Name" },
                { "header.description", "Description" },
                { "detail.name", "name" },
                { "detail.number", "number" },
            };
        }
    }
}