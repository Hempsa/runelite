package net.runelite.client.plugins.banktracker;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Slf4j
public class GraphPanel {
    public JPanel contentPanel;
    private JList itemList;
    private JTextArea searchTextArea;
    private JPanel graphPanel;
    private JRadioButton radioTime7d;
    private JRadioButton radioTime30d;
    private JRadioButton radioTime90d;
    private JRadioButton radioTimeMax;
    private JCheckBox checkMedian;
    private JCheckBox checkInterpolation;

    private BufferedImage graphImage = null;

    private TrackingCollection trackingcollection;
    private List<String> itemNames;
    private List<String> filteredNames;
    private HashSet<String> selectedItems;

    public GraphPanel(TrackingCollection trackingcollection){

        itemList.setModel(new DefaultListModel<String>());
        itemList.setCellRenderer(new SelectedCellRenderer());
        itemList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                String selectedName = filteredNames.get(itemList.getSelectedIndex());
                if(selectedItems.contains(selectedName)){
                    selectedItems.remove(selectedName);
                } else {
                    selectedItems.add(selectedName);
                }
                updateGraph();
            }
        });

        ButtonGroup timeRadioGroup = new ButtonGroup();
        timeRadioGroup.add(radioTime7d);
        timeRadioGroup.add(radioTime30d);
        timeRadioGroup.add(radioTime90d);
        timeRadioGroup.add(radioTimeMax);
        radioTime7d.addActionListener((e)->updateGraph());
        radioTime30d.addActionListener((e)->updateGraph());
        radioTime90d.addActionListener((e)->updateGraph());
        radioTimeMax.addActionListener((e)->updateGraph());

        checkMedian.addActionListener((e) -> updateGraph());
        checkInterpolation.addActionListener((e) -> updateGraph());

        this.trackingcollection = trackingcollection;
        itemNames = new ArrayList<>(trackingcollection.getOverviewMap().keySet());
        itemNames.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        filteredNames = new ArrayList<>(itemNames);

        searchTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updated();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updated();

            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updated();
            }

            private void updated(){
                updateList();
            }
        });

        selectedItems = new HashSet<>();
        updateList();
        updateGraph();
    }

    /**
     * Filters and updates item list based on search
     */
    private void updateList() {
        String text = searchTextArea.getText();
        filteredNames = new ArrayList<>();
        for(String name : itemNames){
            if(name.toLowerCase().contains(text.toLowerCase()))
                filteredNames.add(name);
        }

        DefaultListModel<String> model = (DefaultListModel<String>) itemList.getModel();
        model.removeAllElements();
        for(String name : filteredNames){
            model.addElement(name);
        }
    }

    private void updateGraph() {
        // TODO: handle resizing window somewhere...
        int width = graphPanel.getWidth();
        int height = graphPanel.getHeight();
        if(width <= 0 || height <= 0)
            return;
        graphImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = graphImage.createGraphics();

        g.setColor(Color.GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1));
        for(float rX = 0; rX <= 1.01; rX += 0.1){
            for(float rY = 0; rY <= 1.01; rY += 0.1){
                int x = (int)(width * rX);
                int y = height - (int)(height * rY);
                g.drawLine(0, y, width, y);
                g.drawLine(x, 0, x, height);
            }
        }

        if(selectedItems.size() == 0) {
            g.drawString("Please select some items", width / 2 - 100, height / 2);
            return;
        }

        // Graph bounds
        long now = System.currentTimeMillis();
        long minTime;
        long maxTime = now;
        long dayLength = 24 * 60 * 60 * 1000;
        if(radioTime7d.isSelected()){
            minTime = now - 7 * dayLength; // TODO: UNCOMMENT
        } else if(radioTime30d.isSelected()){
            minTime = now - 30*dayLength;
        } else if(radioTime90d.isSelected()){
            minTime = now - 90*dayLength;
        } else {
            // max
            minTime = Long.MAX_VALUE;
            for(String name : selectedItems){
                Map<Long, Integer> timesToCounts = trackingcollection.getItemCounts(name);
                for(Long time : timesToCounts.keySet()){
                    minTime = Math.min(minTime, time);
                }
            }
        }

        int minCount = 0, maxCount = Integer.MIN_VALUE;

        for(String name : selectedItems){
            Map<Long, Integer> timesToCounts = trackingcollection.getItemCounts(name);
            for(Integer count : timesToCounts.values()){
                maxCount = Math.max(maxCount, count);
            }
        }

        // pad graph top by 10%
        maxCount *= 1.1;

        log.info("times {}, {} \ncounts {}, {}", minTime, maxTime, minCount, maxCount);

        // PLOTTING
        for(String name : selectedItems){
            ArrayList<Long> times = new ArrayList<>();
            Map<Long, Integer> timesToCounts = trackingcollection.getItemCounts(name);
            for(Long time : timesToCounts.keySet()){
                if(time > minTime && time < maxTime)
                    times.add(time);
            }
            times.sort(new Comparator<Long>() {
                @Override
                public int compare(Long o1, Long o2) {
                    return Long.compare(o1, o2);
                }
            });

            ArrayList<Integer> counts = new ArrayList<>();
            for(Long time : times){
                counts.add(timesToCounts.get(time));
            }

            if(checkMedian.isSelected()){
                // TODO: might want to alter the median filter count based on number of observations
                for(int i = 0; i < counts.size(); i++){
                    ArrayList<Integer> windowCounts = new ArrayList<>();
                    for(int j = Math.max(0, i - 2); j < Math.min(counts.size() - 1, j + 2); j++){
                        windowCounts.add(counts.get(j));
                    }
                    windowCounts.sort(new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            return Integer.compare(o1, o2);
                        }
                    });
                    counts.set(i, windowCounts.get(windowCounts.size()/2));
                }
            }
            // TODO: interpolation on counts ^

            int prevX = -1, prevY = -1;
            for(int i = 0; i < times.size(); i++){
                long time = times.get(i);
                int count = counts.get(i);
                // TODO: padding for axes
                double rX = (double)(time - minTime) / (maxTime - minTime);
                double rY = (double)(count - minCount) / (maxCount - minCount);
                int x = (int)(width * rX);
                int y = height - (int)(height * rY);
                log.info("{}({}): {}, {} => rel {}, {} => coord {}, {}", name, i, time, count, rX, rY, x, y);
                // TODO: different color for each item, ALSO SHOW COLORS IN LIST
                g.setColor(Color.green);
                g.fillOval(x-3, y-3, 6, 6);
                if(prevX >= 0){
                    g.drawLine(prevX, prevY, x, y);
                }
                prevX = x;
                prevY = y;
            }
        }
        for(float rX = 0.05f; rX <= 0.91; rX += 0.1){
            for(float rY = 0.05f; rY <= 0.91; rY += 0.1){
                int x = (int)(width * rX);
                int y = height - (int)(height * rY);

                g.setColor(Color.WHITE);
                g.drawString("" + (long)(maxCount * rY), 10, y-30);

                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis((long)((maxTime - minTime) * rX) + minTime);
                String day = "" + cal.get(Calendar.DAY_OF_MONTH);
                String month = "" + (cal.get(Calendar.MONTH) + 1);
                String year = "" + (cal.get(Calendar.YEAR) % 100);
                g.drawString( day + "." + month + "." + year, x + 15, height - 10);
                log.info("{}", rX);
            }
        }

        ((DrawableJPanel)graphPanel).setImage(graphImage);
        graphPanel.repaint();
        itemList.repaint();
    }

    private void createUIComponents() {
        graphPanel = new DrawableJPanel();
    }


    /**
     * List cell renderer that highlights the currently graphed entries
     */
    private class SelectedCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            comp.setBackground(selectedItems.contains(value.toString()) ? Color.GREEN : comp.getBackground());
            return comp;
        }
    }

    private class DrawableJPanel extends JPanel{

        private BufferedImage image = null;
        public void setImage(BufferedImage image){
            this.image = image;
        }

        @Override
        public void paintComponent(Graphics g) {
            if(image == null)
                return;
            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(image, 0,0 , null);
        }
    }
}
