package net.runelite.client.plugins.banktracker;


import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Slf4j
public class Graph {
    private TrackingCollection trackingCollection;
    private Collection<String> itemNames;

    private int minCount = 0;
    private int maxCount = 0; // TODO
    private long minTime = 0; // TODO
    private long maxTime = System.currentTimeMillis();

    private ArrayList<Plot> plots = new ArrayList<>();

    private class Plot {
        String itemName = "";
        Color color = Color.GREEN;
        ArrayList<Double> relativeXs = new ArrayList<Double>();
        ArrayList<Double> relativeYs = new ArrayList<Double>();
        public Plot(String itemName, Color color){
            this.itemName = itemName;
            this.color = color;
        }

        public double getClosestPointDist(double relativeX, double relativeY){
            return 0;
        }

        public double getClosestLinePointDist(double relativeX, double relativeY){
            return 0;
        }
    }

    public Graph(TrackingCollection trackingCollection, Collection<String> itemNames, int timeDays, boolean useMedian){
        this.trackingCollection = trackingCollection;
        this.itemNames = itemNames;

        // Graph bounds
        long now = System.currentTimeMillis();
        maxTime = now;
        long dayLength = 24 * 60 * 60 * 1000;
        if(timeDays > 0){
            minTime = now - timeDays * dayLength;
        } else {
            // max
            minTime = Long.MAX_VALUE;
            for(String name : itemNames){
                Map<Long, Integer> timesToCounts = trackingCollection.getItemCounts(name);
                for(Long time : timesToCounts.keySet()){
                    minTime = Math.min(minTime, time);
                }
            }
        }

        minCount = 0;
        maxCount = Integer.MIN_VALUE;

        for(String name : itemNames){
            Map<Long, Integer> timesToCounts = trackingCollection.getItemCounts(name);
            for(Integer count : timesToCounts.values()){
                maxCount = Math.max(maxCount, count);
            }
        }

        // pad graph top by 10%
        maxCount *= 1.1;

        //log.info("times {}, {} \ncounts {}, {}", minTime, maxTime, minCount, maxCount);

        // PLOTTING
        for(String name : itemNames){
            ArrayList<Long> times = new ArrayList<>();
            Map<Long, Integer> timesToCounts = trackingCollection.getItemCounts(name);
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

            if(useMedian){
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

            Plot plot = new Plot(name, Color.GREEN); // TODO different colors, display also in list
            for(int i = 0; i < times.size(); i++){
                long time = times.get(i);
                int count = counts.get(i);
                double rX = (double)(time - minTime) / (maxTime - minTime);
                double rY = (double)(count - minCount) / (maxCount - minCount);
                plot.relativeXs.add(rX);
                plot.relativeYs.add(rY);
            }
            plots.add(plot);
        }
    }

    /**
     * Generates image for graph
     * @param width canvas width
     * @param height canvas height
     */
    public BufferedImage getImage(int width, int height){
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();

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

        if(itemNames.size() == 0) {
            g.drawString("Please select some items", width / 2 - 100, height / 2);
            return null;
        }

        for(Plot plot : plots){
            g.setColor(plot.color);
            int prevX = -1, prevY = -1;
            for(int i = 0; i < plot.relativeXs.size(); i++){
                double rX = plot.relativeXs.get(i);
                double rY = plot.relativeYs.get(i);
                int x = (int)(width * rX);
                int y = height - (int)(height * rY);
                //log.info("{}({}): {}, {} => rel {}, {} => coord {}, {}", name, i, time, count, rX, rY, x, y);
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


                long time = ((long)((maxTime - minTime) * rX) + minTime);
                g.drawString( getTimeStr(time), x + 15, height - 10);
                //log.info("{}", rX);
            }
        }
        return result;
    }

    public class PlotInfoElement {
        String itemName = "";
        Color color = Color.GREEN;
        double relativeX = 0;
        double relativeY = 0;
        boolean isEstimate = true;

        public PlotInfoElement(String itemName, Color color, double relativeX, double relativeY, boolean isEstimate) {
            this.itemName = itemName;
            this.color = color;
            this.relativeX = relativeX;
            this.relativeY = relativeY;
            this.isEstimate = isEstimate;
        }
    }

    public PlotInfoElement getInfoForPoint(double relativeX, double relativeY){
        final double MAX_DIST_SQ = 0.075 * 0.075;
        final double LINE_BIAS = 10; // distance to lines is inflated to make lines less valuable

        double minDistSq = Double.MAX_VALUE;
        PlotInfoElement closestInfo = null;
        for(Plot plot : plots){
            for(int i = 0; i < plot.relativeXs.size(); i++){
                double rX = plot.relativeXs.get(i);
                double rY = plot.relativeYs.get(i);
                double distSq = (rX - relativeX)*(rX - relativeX) + (rY - relativeY) * (rY - relativeY);
                if (distSq < minDistSq && distSq < MAX_DIST_SQ){
                    minDistSq = distSq;
                    closestInfo = new PlotInfoElement(plot.itemName, plot.color, rX, rY, false);
                }

                if(i > 0){
                    double rXp = plot.relativeXs.get(i-1);
                    double rYp = plot.relativeYs.get(i-1);
                    // only search the line segment if the x-coordinate of search point is within it
                    if(relativeX > rXp && relativeX < rX) {

                        double slope = 0.01;
                        // divide by zero fix (probably does not work properly)
                        if (Math.abs(rX - rXp) > 0.01)
                            slope = (rY - rYp) / (rX - rXp);
                        double intercept = rYp - slope * rXp;

                        double slope2 = -100000;
                        if (Math.abs(slope) > 0.01)
                            slope2 = -1 / slope;
                        double intercept2 = relativeY - slope2 * relativeX;

                        double intersectX = (intercept2 - intercept) / (slope - slope2);
                        double intersectY = slope2 * intersectX + intercept2;

                        distSq = (intersectX - relativeX) * (intersectX - relativeX) + (intersectY - relativeY) * (intersectY - relativeY);
                        distSq *= LINE_BIAS;
                        if (distSq < minDistSq && distSq < MAX_DIST_SQ) {
                            //log.info("intersect @ {} {}", intersectX, intersectY);
                            //log.info("range @ {} {} - {} {}", rXp, rYp, rX, rY);
                            //log.info("slopes {} {}", slope, slope2);
                            minDistSq = distSq;
                            closestInfo = new PlotInfoElement(plot.itemName, plot.color, intersectX, intersectY, true);
                        }
                    }
                }
            }
        }
        return closestInfo;
    }

    /**
     * @param relativeY 0...1 value from canvas LEFT border
     */
    public int getCount(double relativeY){
        return (int)(relativeY * (maxCount - minCount) + minCount);
    }

    /**
     * @param relativeX 0...1 value from canvas BOTTOM border (NOT TOP!)
     */
    public long getTime(double relativeX){
        return (long)(relativeX * (maxTime - minTime)) + minTime;
    }

    /**
     * @param relativeX 0...1 value from canvas BOTTOM border (NOT TOP!)
     */
    public String getTimeStr(double relativeX){
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis((long)((maxTime - minTime) * relativeX) + minTime);
        String day = "" + cal.get(Calendar.DAY_OF_MONTH);
        String month = "" + (cal.get(Calendar.MONTH) + 1);
        String year = "" + (cal.get(Calendar.YEAR) % 100);
        return day + "." + month + "." + year;
    }
}
