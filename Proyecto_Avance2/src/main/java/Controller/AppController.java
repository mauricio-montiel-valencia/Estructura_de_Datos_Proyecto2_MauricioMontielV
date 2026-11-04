package Controller;

import Model.Airport;
import Model.CriticalRoute;
import Model.Graph;
import Model.GraphLoader;
import Model.PathFinder;
import Model.ReachabilityAnalyzer;
import Model.Route;
import Model.RouteCriterion;
import Model.RoutePath;
import View.MainWindow;
import View.MainWindowListener;
import View.ReportFormatter;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class AppController implements MainWindowListener {

    private static final String[] DEFAULT_FOLDERS = {".", "datos", "data"};
    private static final String AIRPORTS_FILE = "aeropuertos.txt";
    private static final String ROUTES_FILE = "rutas.txt";
    private static final String DEFAULT_ROUTE_MESSAGE ="Type an origin and a destination (or click on the airports),\nchoose the criterion and press «Search route».";

    private final GraphLoader loader;
    private final MainWindow view;

    private Graph graph = new Graph();
    private PathFinder pathFinder = new PathFinder(graph);
    private ReachabilityAnalyzer analyzer = new ReachabilityAnalyzer(graph);

    private RoutePath currentPath = null;                       
    private RouteCriterion currentCriterion = RouteCriterion.FEWEST_STOPS;
    private String routeMessage = DEFAULT_ROUTE_MESSAGE;
    private boolean[] reachable = null;                         
    private int reachableOrigin = -1;
    private boolean choosingOrigin = true;                      

    public AppController(GraphLoader loader, MainWindow view) {
        
        this.loader = loader;
        this.view = view;
        view.setListener(this);
        view.setGraph(graph);
        updateRoutePanel();
    }

    public void start() {
        
        for (String folder : DEFAULT_FOLDERS) {
            File airports = new File(folder, AIRPORTS_FILE), routes = new File(folder, ROUTES_FILE);
            if (airports.exists() && routes.exists() && tryLoad(airports, routes)) return;
        }
        view.setStatus(AIRPORTS_FILE + " and " + ROUTES_FILE + " were not found. Use «Load files...».");
    }

    private boolean tryLoad(File airportsFile, File routesFile) {
        
        Graph loaded;
        
        try {loaded = loader.load(airportsFile, routesFile);}
        catch (IOException ex) {view.showLoadError(ex.getMessage());return false;}
        
        graph = loaded;
        pathFinder = new PathFinder(graph);
        analyzer = new ReachabilityAnalyzer(graph);
        view.setGraph(graph);
        resetState();
        StringBuilder sb = new StringBuilder(graph.size() + " airports and " + graph.getRouteCount() + " routes loaded.");
       
        for (String w : graph.getWarnings()) sb.append("\nWarning: ").append(w);
        showQuery(sb.toString());
        return true;
    }

    @Override
    public void onLoadFiles() {
        File[] files = view.chooseDataFiles();
        if (files != null) tryLoad(files[0], files[1]);
    }

    private void resetState() {
        
        currentPath = null;
        reachable = null;
        reachableOrigin = -1;
        choosingOrigin = true;
        view.setOriginText("");
        view.setDestinationText("");
        routeMessage = DEFAULT_ROUTE_MESSAGE;
        updateRoutePanel();
        refreshMap();
    }

    @Override
    public void onSearchRoute() {
        reachable = null;
        reachableOrigin = -1;
        currentPath = null;

        String originCode = normalized(view.getOriginText());
        String destinationCode = normalized(view.getDestinationText());
        int o = graph.indexOf(originCode), d = graph.indexOf(destinationCode);
        if (originCode.isEmpty() || destinationCode.isEmpty()) {routeMessage = "Type the origin and the destination.";}
        else if (o < 0) {routeMessage = "The code \"" + originCode + "\" does not exist.";}
        else if (d < 0) {routeMessage = "The code \"" + destinationCode + "\" does not exist.";}
        else if (o == d) {routeMessage = "Origin and destination are the same airport (" + originCode + "): there is no flight to make.";}
        else {
            currentCriterion = view.getSelectedCriterion();
            currentPath = pathFinder.find(o, d, currentCriterion);
            if (currentPath == null) routeMessage = "No route exists from " + originCode + " to " + destinationCode + ".";
        }
        view.setStatus(currentPath != null
                ? currentCriterion.getLabel() + ": " + ReportFormatter.pathText(graph, currentPath)
                : firstLine(routeMessage));
        updateRoutePanel();
        refreshMap();
    }

    @Override
    public void onCriterionChanged() {if (currentPath != null) onSearchRoute();}

    @Override
    public void onAirportClicked(int index) {
        
        Airport a = graph.getAirport(index);
        
        if (choosingOrigin) {
            
            view.setOriginText(a.getCode());
            view.setDestinationText("");
            currentPath = null;
            reachable = null;
            reachableOrigin = -1;
            routeMessage = "Origin: " + a.getCity() + " (" + a.getCode() + "). Now choose the destination.";
            view.setStatus(routeMessage);
            updateRoutePanel();
        } 
        else {view.setDestinationText(a.getCode());onSearchRoute();}
        
        choosingOrigin = !choosingOrigin;
        refreshMap();
    }

    @Override
    public void onAirportInfo() {
        
        String code = view.askAirportCode(normalized(view.getOriginText()));
        
        if (code == null) return;
        
        code = normalized(code);
        int i = graph.indexOf(code);
        
        if (i < 0) {showQuery("The code \"" + code + "\" does not exist.");return;}
        showQuery(ReportFormatter.airportInfo(graph, i));
    }

    @Override
    public void onReachableAirports() {
        
        String code = normalized(view.getOriginText());
        int o = graph.indexOf(code);
        if (o < 0) {
            showQuery(code.isEmpty() ? "Type the origin to query the reachable airports."
                                     : "The code \"" + code + "\" does not exist.");
            return;
        }
        if (reachable != null && reachableOrigin == o) {  
            reachableOrigin = -1;
            showQuery("Reachability colors hidden.");
            refreshMap();
            return;
        }
        currentPath = null;
        routeMessage = "Reachability query active (see the queries panel).";
        updateRoutePanel();
        reachable = analyzer.reachableFrom(o);
        reachableOrigin = o;
        showQuery(ReportFormatter.reachability(graph, reachable, o));
        refreshMap();
    }

    @Override
    public void onCriticalRoutes() {
        
        String code = normalized(view.getOriginText());
        int o = graph.indexOf(code);
        
        if (o < 0) {
            showQuery(code.isEmpty() ? "Type the origin to look for critical routes."
                                     : "The code \"" + code + "\" does not exist.");
            return;
        }
        List<CriticalRoute> critical = analyzer.findCriticalRoutes(o);
        showQuery(ReportFormatter.criticalRoutes(graph, o, critical));
    }

    @Override
    public void onCancelRoute() {
        
        String originCode = normalized(view.getOriginText()), destinationCode = normalized(view.getDestinationText());
        int o = graph.indexOf(originCode), d = graph.indexOf(destinationCode);
        if (o < 0 || d < 0) {showQuery("Type two valid codes (origin and destination) to cancel the route.");return;}
        if (!graph.cancelRoutes(o, d)) {
            showQuery("There is no direct route " + originCode + " → " + destinationCode + " to cancel.");
            return;
        }
        refreshAfterGraphChange("Route cancelled: " + originCode + " → " + destinationCode
                + " (in memory only; the file is not modified).\nCancelled routes: " + graph.getCancelledRoutes().size());
    }

    @Override
    public void onRestoreRoutes() {
        
        int k = graph.getCancelledRoutes().size();
        graph.restoreRoutes();
        refreshAfterGraphChange(k == 0 ? "There were no cancelled routes." : k + " cancelled routes were restored.");
    }

    private void refreshAfterGraphChange(String note) {
        
        String extra = "";
        
        if (reachable != null) {
            reachable = analyzer.reachableFrom(reachableOrigin);
            extra = "\n\n" + ReportFormatter.reachability(graph, reachable, reachableOrigin);
        }
        
        showQuery(note + extra);
        
        if (currentPath != null) onSearchRoute();
        refreshMap();
    }

    private static String normalized(String code) {return code.trim().toUpperCase();}

    private static String firstLine(String text) {return text.split("\n")[0];}

    private void showQuery(String text) {
        
        view.showQuery(text);
        view.setStatus(firstLine(text));
    }

    private void updateRoutePanel() {
        
        if (currentPath == null) view.showRouteMessage(routeMessage);
        else view.showRoutePath(graph, currentPath, currentCriterion);
    }

    private void refreshMap() {
        view.updateMap(currentPath, reachable, reachableOrigin, graph.indexOf(view.getOriginText()), graph.indexOf(view.getDestinationText()));
    }
}