package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    private final List<Airport> airports = new ArrayList<>();
    private final Map<String, Integer> indexByCode = new HashMap<>();
    private final List<List<Route>> adjacency = new ArrayList<>();
    private final List<Route> originalRoutes = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public boolean addAirport(Airport airport) {
        
        if (indexByCode.containsKey(airport.getCode())) return false;
        
        indexByCode.put(airport.getCode(), airports.size());
        airports.add(airport);
        adjacency.add(new ArrayList<>());
       
        return true;
    }

    public void addRoute(Route route) {
        
        originalRoutes.add(route);
        adjacency.get(route.getOrigin()).add(route);
    }

    public void addWarning(String warning) {warnings.add(warning);}

    public int size() {return airports.size();}

    public Airport getAirport(int index) {return airports.get(index);}

    public int indexOf(String code) {
        
        if (code == null) return -1;
        
        Integer i = indexByCode.get(code.trim().toUpperCase());
        return i == null ? -1 : i;
    }

    public List<Route> getOutgoingRoutes(int airport) {return Collections.unmodifiableList(adjacency.get(airport));}

    public int getRouteCount() {return originalRoutes.size();}

    public List<String> getWarnings() {return Collections.unmodifiableList(warnings);}
}
