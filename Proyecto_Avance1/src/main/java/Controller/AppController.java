package Controller;

import Model.Graph;
import Model.GraphLoader;
import Model.PathFinder;
import Model.RouteCriterion;
import Model.RoutePath;
import View.ConsoleView;
import java.io.File;
import java.io.IOException;

public class AppController {

    private static final String[] DEFAULT_FOLDERS = {".", "datos", "data"};
    private static final String AIRPORTS_FILE = "aeropuertos.txt";
    private static final String ROUTES_FILE = "rutas.txt";

    private final GraphLoader loader;
    private final ConsoleView view;
    private Graph graph = new Graph();
    private PathFinder pathFinder = new PathFinder(graph);

    public AppController(GraphLoader loader, ConsoleView view) {
        
        this.loader = loader;
        this.view = view;
    }

    public void run() {
        
        if (loadDefaultFiles()) {view.showLoaded(graph);view.showWarnings(graph);}
        else {view.showFilesNotFound();}
        
        menuLoop();
    }

    private void setGraph(Graph newGraph) {
        
        graph = newGraph;
        pathFinder = new PathFinder(graph);
    }

    private boolean loadDefaultFiles() {
        
        for (String folder : DEFAULT_FOLDERS) {
            
            File airports = new File(folder, AIRPORTS_FILE), routes = new File(folder, ROUTES_FILE);
            
            if (airports.exists() && routes.exists()) {
                
                try {setGraph(loader.load(airports, routes));return true;}
                catch (IOException ex) {view.showLoadError(folder, ex.getMessage());}
            }
        }
        return false;
    }

    private void menuLoop() {
        
        while (true) {
            
            view.showMenu();
            String option = view.readOption();
            if (option == null || option.equals("0")) return;
            
            switch (option) {
                
                case "1": view.showAirports(graph); break;
                
                case "2": queryRoute(RouteCriterion.FEWEST_STOPS); break;
                
                case "3": queryRoute(RouteCriterion.FASTEST); break;
                
                case "4": queryRoute(RouteCriterion.CHEAPEST); break;
                
                case "5":if (!loadOtherFiles()) return;break;
                
                default: view.showMessage("Invalid option.");
            }
        }
    }

    private void queryRoute(RouteCriterion criterion) {
        
        String originCode = view.readLine("Origin (code): ");
        
        if (originCode == null) return;
        
        String destinationCode = view.readLine("Destination (code): ");
        
        if (destinationCode == null) return;

        int o = graph.indexOf(originCode), d = graph.indexOf(destinationCode);
        
        if (o < 0) { view.showUnknownCode(originCode); return; }
        if (d < 0) { view.showUnknownCode(destinationCode); return; }
        if (o == d) { view.showSameAirport(); return; }

        RoutePath path = pathFinder.find(o, d, criterion);
        
        if (path == null) view.showNoRoute(graph, o, d);
        else view.showPath(graph, path, criterion);
    }

    private boolean loadOtherFiles() {
        
        String airports = view.readLine("Path to the airports file: ");
        String routes = view.readLine("Path to the routes file: ");
        
        if (airports == null || routes == null) return false;
        
        try {setGraph(loader.load(new File(airports), new File(routes)));view.showLoaded(graph);}
        catch (IOException ex) {view.showMessage("The files could not be loaded: " + ex.getMessage());}
        
        return true;
    }
}