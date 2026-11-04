package Model;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class GraphLoader {

    private static String nextToken(Scanner sc, String what) throws IOException {
        
        if (!sc.hasNext()) throw new IOException("Unexpected end of file: " + what + " is missing.");
        return sc.next().replace("\uFEFF", "");
    }

    private static int nextInt(Scanner sc, String what) throws IOException {
        
        String s = nextToken(sc, what);
        
        try {return Integer.parseInt(s);}
        catch (NumberFormatException ex) {throw new IOException("An integer was expected for " + what + " but found \"" + s + "\".");}
    }

    private static double nextDouble(Scanner sc, String what) throws IOException {
        
        String s = nextToken(sc, what);
        
        try {return Double.parseDouble(s);}
        catch (NumberFormatException ex) {throw new IOException("A number was expected for " + what + " but found \"" + s + "\".");}
    }

    public Graph load(File airportsFile, File routesFile) throws IOException {
        
        Graph graph = new Graph();

        try (Scanner sc = new Scanner(airportsFile, "UTF-8")) {
            
            int count = nextInt(sc, "the airport count");
            
            for (int i = 0; i < count; i++) {
                
                String code = nextToken(sc, "the code of airport " + (i + 1)).toUpperCase();
                double x = nextDouble(sc, "x of " + code);
                double y = nextDouble(sc, "y of " + code);
                String city = nextToken(sc, "the city of " + code).replace('_', ' ');
                String country = nextToken(sc, "the country of " + code).replace('_', ' ');
                
                if (!graph.addAirport(new Airport(code, city, country, x, y))) {graph.addWarning("Duplicate airport (ignored): " + code);}
            }
        }

        try (Scanner sc = new Scanner(routesFile, "UTF-8")) {
            
            int count = nextInt(sc, "the route count");
            
            for (int i = 0; i < count; i++) {
                
                String o = nextToken(sc, "the origin of route " + (i + 1)).toUpperCase();
                String d = nextToken(sc, "the destination of route " + (i + 1)).toUpperCase();
                double minutes = nextDouble(sc, "the minutes of " + o + " " + d);
                double cost = nextDouble(sc, "the cost of " + o + " " + d);
                int io = graph.indexOf(o), id = graph.indexOf(d);
                
                if (io < 0 || id < 0) {graph.addWarning("Route ignored (unknown code): " + o + " " + d); continue;}
                if (minutes < 0 || cost < 0) {graph.addWarning("Route with negative weight (" + o + " " + d+ "): Dijkstra is not valid with negative weights.");}
                graph.addRoute(new Route(io, id, minutes, cost));
            }
        }
        return graph;
    }
}