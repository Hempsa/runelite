package net.runelite.client.plugins.banktracker;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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

    private Graph graph = null;
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

        graphPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                graphMouseMoved(e.getX(), e.getY());
            }
        });

        selectedItems = new HashSet<>();
        updateList();
        updateGraph();
    }

    private void createUIComponents() {
        graphPanel = new DrawableJPanel();
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

        int timeDays = 0; // default: max
        if(radioTime7d.isSelected())
            timeDays = 7;
        else if(radioTime30d.isSelected())
            timeDays = 30;
        else if(radioTime90d.isSelected())
            timeDays = 90;

        graph = new Graph(trackingcollection, selectedItems, timeDays, checkMedian.isSelected());
        graphImage = graph.getImage(width, height);
        ((DrawableJPanel)graphPanel).setImage(graphImage);
        graphPanel.repaint();
        itemList.repaint();
    }

    private void graphMouseMoved(int x, int y){
        if(graph == null || graphImage == null)
            return;
        //log.info("{} {}", x, y);
        double relativeX = ((double)x / graphPanel.getWidth());
        double relativeY = ((double)(graphPanel.getHeight() - y) / graphPanel.getHeight());
        //log.info("=> {} {}", relativeX, relativeY);

        Graph.PlotInfoElement info = graph.getInfoForPoint(relativeX, relativeY);
        if(info == null) {
            ((DrawableJPanel)graphPanel).setImage(graphImage);
        }else {
            //log.info("info found @ {} {}", info.relativeX, info.relativeY);
            BufferedImage img = new BufferedImage(graphImage.getWidth(), graphImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.drawImage(graphImage, 0, 0, null);

            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            int foundX = (int) (graphPanel.getWidth() * info.relativeX);
            int foundY = (int) (graphPanel.getHeight() - graphPanel.getHeight() * info.relativeY);
            g.drawLine(foundX, foundY - 8, foundX, foundY + 8);

            g.setColor(info.color);
            g.drawString(info.itemName, x - 60, y - 30);
            String countString = info.isEstimate ? "~" : "";
            countString += graph.getCount(info.relativeY);
            g.drawString(countString, x - 60, y - 15);
            String timeString = info.isEstimate ? "~" : "";
            timeString += graph.getTimeStr(info.relativeX);
            g.drawString(timeString, x - 60, y);
            ((DrawableJPanel)graphPanel).setImage(img);
        }
        graphPanel.repaint();
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
