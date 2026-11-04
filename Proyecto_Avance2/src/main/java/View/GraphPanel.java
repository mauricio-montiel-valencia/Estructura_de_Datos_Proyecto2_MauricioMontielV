package View;

import Model.Airport;
import Model.Graph;
import Model.Route;
import Model.RoutePath;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.function.IntConsumer;

public class GraphPanel extends JPanel {

    private static final int MARGIN = 40;
    private static final double PICK_RADIUS = 12;

    private Graph graph = new Graph();
    private RoutePath path;                 
    private boolean[] reachable;            
    private int reachableOrigin = -1;
    private int selectedOrigin = -1, selectedDestination = -1;
    private int hover = -1;
    private final IntConsumer onAirportClicked;

    public GraphPanel(IntConsumer onAirportClicked) {
        
        this.onAirportClicked = onAirportClicked;
        setBackground(new Color(20, 60, 110));
        
        MouseAdapter mouse = new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = airportAt(e.getX(), e.getY());
                if (i >= 0) GraphPanel.this.onAirportClicked.accept(i);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                
                int h = airportAt(e.getX(), e.getY());
                
                if (h != hover) {
                    hover = h;
                    setCursor(h >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    Airport a = h >= 0 ? graph.getAirport(h) : null;
                    setToolTipText(a == null ? null : a.getCode() + " - " + a.getCity() + ", " + a.getCountry());
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
        this.hover = -1;
        repaint();
    }

    public void setState(RoutePath path, boolean[] reachable, int reachableOrigin,int selectedOrigin, int selectedDestination) {
        this.path = path;
        this.reachable = reachable;
        this.reachableOrigin = reachableOrigin;
        this.selectedOrigin = selectedOrigin;
        this.selectedDestination = selectedDestination;
        repaint();
    }

    private double px(int i, int w) {return MARGIN + graph.getAirport(i).getX() / 1000.0 * (w - 2.0 * MARGIN);}
    private double py(int i, int h) {return MARGIN + graph.getAirport(i).getY() / 1000.0 * (h - 2.0 * MARGIN);}

    private int airportAt(int mx, int my) {
        
        int w = getWidth(), h = getHeight();
        int best = -1;
        double bestDistance = PICK_RADIUS;
        
        for (int i = 0; i < graph.size(); i++) {
            
            double d = Math.hypot(mx - px(i, w), my - py(i, h));
            
            if (d < bestDistance) { bestDistance = d; best = i; }
        }
        return best;
    }

    private void arrow(Graphics2D g, double x0, double y0, double x1, double y1, double f) {
        
        double qx = x0 + (x1 - x0) * f, qy = y0 + (y1 - y0) * f;
        double angle = Math.atan2(y1 - y0, x1 - x0);
        Path2D.Double p = new Path2D.Double();
        
        p.moveTo(qx + 8 * Math.cos(angle), qy + 8 * Math.sin(angle));
        p.lineTo(qx + 7 * Math.cos(angle + 2.5), qy + 7 * Math.sin(angle + 2.5));
        p.lineTo(qx + 7 * Math.cos(angle - 2.5), qy + 7 * Math.sin(angle - 2.5));
        p.closePath();
        g.fill(p);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        int w = getWidth(), h = getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (graph.size() == 0) {
            
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(Color.WHITE);
            String s = "Load aeropuertos.txt and rutas.txt with «Load files...»";
            g.drawString(s, (w - g.getFontMetrics().stringWidth(s)) / 2, h / 2);
            return;
        }

        g.setStroke(new BasicStroke(1f));
        g.setColor(new Color(255, 255, 255, 70));
        
        for (int u = 0; u < graph.size(); u++)
            for (Route r : graph.getOutgoingRoutes(u))
                
                g.draw(new Line2D.Double(px(u, w), py(u, h), px(r.getDestination(), w), py(r.getDestination(), h)));

        if (hover >= 0) {
            
            g.setStroke(new BasicStroke(1.6f));
            g.setColor(new Color(150, 230, 255, 200));
            for (Route r : graph.getOutgoingRoutes(hover))
                g.draw(new Line2D.Double(px(hover, w), py(hover, h), px(r.getDestination(), w), py(r.getDestination(), h)));
        }
        
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[]{5f, 5f}, 0f));
        g.setColor(new Color(255, 70, 70));
        for (Route r : graph.getCancelledRoutes())
            g.draw(new Line2D.Double(px(r.getOrigin(), w), py(r.getOrigin(), h), px(r.getDestination(), w), py(r.getDestination(), h)));

        boolean[] onPath = new boolean[graph.size()];
        
        if (path != null) {
            
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            for (Route r : path.getLegs()) {
                
                int o = r.getOrigin(), d = r.getDestination();
                onPath[o] = true;
                onPath[d] = true;
                g.setColor(new Color(255, 215, 60, 220));
                g.draw(new Line2D.Double(px(o, w), py(o, h), px(d, w), py(d, h)));
                
                if (Math.hypot(px(d, w) - px(o, w), py(d, h) - py(o, h)) > 30) arrow(g, px(o, w), py(o, h), px(d, w), py(d, h), 0.6);
            }
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        
        for (int i = 0; i < graph.size(); i++) {
            
            double x = px(i, w), y = py(i, h);
            Color fill = Color.WHITE;
            
            if (reachable != null) fill = (i == reachableOrigin) ? new Color(60, 120, 255) : reachable[i] ? new Color(60, 200, 80) : new Color(225, 50, 50);
            else if (onPath[i]) fill = new Color(255, 215, 60);
            
            double radius = (i == hover) ? 7.5 : 5.5;
            
            if (i == selectedOrigin || i == selectedDestination) {
                
                g.setColor(i == selectedOrigin ? new Color(0, 120, 255) : new Color(255, 140, 0));
                g.setStroke(new BasicStroke(2.5f));
                g.draw(new Ellipse2D.Double(x - radius - 4, y - radius - 4, 2 * (radius + 4), 2 * (radius + 4)));
            }
            
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(fill);
            g.fill(new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius));
            g.setColor(new Color(30, 30, 30));
            g.draw(new Ellipse2D.Double(x - radius, y - radius, 2 * radius, 2 * radius));
            g.setColor(Color.WHITE);
            g.drawString(graph.getAirport(i).getCode(), (int) x + 8, (int) y - 6);
        }
    }
}