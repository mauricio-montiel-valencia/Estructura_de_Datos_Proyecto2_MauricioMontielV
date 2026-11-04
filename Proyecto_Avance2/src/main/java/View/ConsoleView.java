package View;

import Model.Airport;
import Model.Graph;
import Model.Route;
import Model.RouteCriterion;
import Model.RoutePath;
import java.util.Scanner;

public class ConsoleView {

    private final Scanner in = new Scanner(System.in);

    public String readLine(String prompt) {
        
        System.out.print(prompt);
        if (!in.hasNextLine()) return null;
        return in.nextLine().trim();
    }

    public String readOption() {return readLine("Option: ");}

    public void showMenu() {
        
        System.out.println("\n===== AIR ROUTE PLANNER (Advance 1) =====");
        System.out.println("1. List airports");
        System.out.println("2. Route with fewest stops (BFS)");
        System.out.println("3. Fastest route (Dijkstra)");
        System.out.println("4. Cheapest route (Dijkstra)");
        System.out.println("5. Load other files");
        System.out.println("0. Exit");
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showWarnings(Graph graph) {for (String w : graph.getWarnings()) System.out.println("Warning: " + w);}

    public void showLoaded(Graph graph) {
        System.out.println(graph.size() + " airports and " + graph.getRouteCount() + " routes loaded.");
    }

    public void showFilesNotFound() {
        System.out.println("aeropuertos.txt and rutas.txt were not found. Use option 5 to specify them.");
    }

    public void showLoadError(String where, String detail) {
        System.out.println("Error loading " + where + ": " + detail);
    }

    public void showUnknownCode(String code) {
        System.out.println("The code \"" + code.toUpperCase() + "\" does not exist.");
    }

    public void showSameAirport() {
        System.out.println("Origin and destination are the same airport.");
    }

    public void showNoRoute(Graph graph, int origin, int destination) {
        System.out.println("No route exists from " + graph.getAirport(origin).getCode() + " to " + graph.getAirport(destination).getCode() + ".");
    }

    public void showAirports(Graph graph) {
        
        System.out.println("\nAIRPORTS (" + graph.size() + ")");
        
        for (int i = 0; i < graph.size(); i++) {
            Airport a = graph.getAirport(i);
            System.out.printf("%-5s %-25s %-20s outgoing routes: %d%n", a.getCode(), a.getCity(), a.getCountry(), graph.getOutgoingRoutes(i).size());
        }
        
        showWarnings(graph);
    }

    public void showPath(Graph graph, RoutePath path, RouteCriterion criterion) {
        
        StringBuilder sb = new StringBuilder(graph.getAirport(path.getOrigin()).getCode());
        
        for (Route r : path.getLegs()) sb.append(" → ").append(graph.getAirport(r.getDestination()).getCode());
        
        double totalMinutes = path.getTotalMinutes();
        int tm = (int) Math.round(totalMinutes);
        System.out.println("\nCRITERION: " + criterion.getLabel());
        System.out.println("Route:   " + sb);
        System.out.println("Flights: " + path.getFlightCount() + "   Stops: " + path.getStopCount());
        System.out.printf("Time:    %s min (%dh %02dm)%n", format(totalMinutes), tm / 60, tm % 60);
        System.out.println("Cost:    $" + format(path.getTotalCost()) + " USD");
    }

    private static String format(double v) {return v == Math.rint(v) ? String.valueOf((long) v) : String.format("%.1f", v);}
}