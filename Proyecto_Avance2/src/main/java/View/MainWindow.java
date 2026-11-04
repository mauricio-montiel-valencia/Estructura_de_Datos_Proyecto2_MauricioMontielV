package View;

import Model.Graph;
import Model.RouteCriterion;
import Model.RoutePath;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;

public class MainWindow extends JFrame {

    private final GraphPanel graphPanel;
    private final JTextField originField = new JTextField(5);
    private final JTextField destinationField = new JTextField(5);
    private final JComboBox<String> criterionBox;
    private final JTextArea routeArea = new JTextArea();
    private final JTextArea queryArea = new JTextArea();
    private final JLabel statusLabel = new JLabel(" Loading data...");
    private MainWindowListener listener;

    public MainWindow() {
        
        super("Air Route Planner - Americas (Advance 2)");
        setLayout(new BorderLayout());

        String[] labels = new String[RouteCriterion.values().length];
        for (int i = 0; i < labels.length; i++) labels[i] = RouteCriterion.values()[i].getLabel();
        criterionBox = new JComboBox<>(labels);

        JButton searchButton = new JButton("Search route");
        JButton reachableButton = new JButton("Reachable (DFS)");
        JButton airportButton = new JButton("Query airport");
        JButton cancelButton = new JButton("Cancel route");
        JButton restoreButton = new JButton("Restore routes");
        JButton criticalButton = new JButton("Critical routes");
        JButton loadButton = new JButton("Load files...");

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Origin:"));
        row1.add(originField);
        row1.add(new JLabel("Destination:"));
        row1.add(destinationField);
        row1.add(new JLabel("Criterion:"));
        row1.add(criterionBox);
        row1.add(searchButton);
        row1.add(new JLabel("   Click two airports: origin and destination"));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(reachableButton);
        row2.add(airportButton);
        row2.add(cancelButton);
        row2.add(restoreButton);
        row2.add(criticalButton);
        row2.add(loadButton);

        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(row1);
        north.add(row2);
        add(north, BorderLayout.NORTH);

        graphPanel = new GraphPanel(i -> listener.onAirportClicked(i));
        add(graphPanel, BorderLayout.CENTER);

        JPanel right = new JPanel(new GridLayout(2, 1));
        right.setPreferredSize(new Dimension(340, 0));
        right.add(textPanel("Route panel", routeArea));
        right.add(textPanel("Queries", queryArea));
        add(right, BorderLayout.EAST);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(statusLabel, BorderLayout.SOUTH);

        originField.addActionListener(e -> listener.onSearchRoute());
        destinationField.addActionListener(e -> listener.onSearchRoute());
        searchButton.addActionListener(e -> listener.onSearchRoute());
        criterionBox.addActionListener(e -> listener.onCriterionChanged());
        reachableButton.addActionListener(e -> listener.onReachableAirports());
        airportButton.addActionListener(e -> listener.onAirportInfo());
        cancelButton.addActionListener(e -> listener.onCancelRoute());
        restoreButton.addActionListener(e -> listener.onRestoreRoutes());
        criticalButton.addActionListener(e -> listener.onCriticalRoutes());
        loadButton.addActionListener(e -> listener.onLoadFiles());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 560));
        setLocationRelativeTo(null);
    }

    private JScrollPane textPanel(String title, JTextArea area) {
        
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }

    public void setListener(MainWindowListener listener) {this.listener = listener;}

    public String getOriginText() {return originField.getText();}

    public String getDestinationText() {return destinationField.getText();}

    public RouteCriterion getSelectedCriterion() {return RouteCriterion.values()[Math.max(0, criterionBox.getSelectedIndex())];}

    public void setOriginText(String text) {originField.setText(text);}

    public void setDestinationText(String text) {destinationField.setText(text);}

    public void setStatus(String text) {statusLabel.setText(" " + text);}

    public void showRouteMessage(String message) {
        routeArea.setText(message + "\n");
        routeArea.setCaretPosition(0);
    }

    public void showRoutePath(Graph graph, RoutePath path, RouteCriterion criterion) {
        routeArea.setText(ReportFormatter.routeDetails(graph, path, criterion));
        routeArea.setCaretPosition(0);
    }

    public void showQuery(String text) {
        queryArea.setText(text);
        queryArea.setCaretPosition(0);
    }

    public void setGraph(Graph graph) {
        graphPanel.setGraph(graph);
    }

    public void updateMap(RoutePath path, boolean[] reachable, int reachableOrigin, int selectedOrigin, int selectedDestination) {
        
        graphPanel.setState(path, reachable, reachableOrigin, selectedOrigin, selectedDestination);
    }

    public String askAirportCode(String defaultCode) {return JOptionPane.showInputDialog(this, "Airport code (for example SJO):", defaultCode);}

    public File[] chooseDataFiles() {
        
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Select the AIRPORTS file (aeropuertos.txt)");
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        
        File airports = chooser.getSelectedFile();
        chooser.setDialogTitle("Select the ROUTES file (rutas.txt)");
        
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return null;
        
        return new File[]{airports, chooser.getSelectedFile()};
    }

    public void showLoadError(String detail) {
        JOptionPane.showMessageDialog(this, "The files could not be loaded:\n" + detail,"Load error", JOptionPane.ERROR_MESSAGE);
    }
}
