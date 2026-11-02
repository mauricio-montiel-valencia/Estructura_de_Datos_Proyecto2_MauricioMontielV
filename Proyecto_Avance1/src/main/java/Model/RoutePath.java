package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoutePath {

    private final List<Route> legs;

    public RoutePath(List<Route> legs) {this.legs = Collections.unmodifiableList(new ArrayList<>(legs));}

    public List<Route> getLegs() {return legs;}

    public int getOrigin() {return legs.get(0).getOrigin();}

    public int getFlightCount() {return legs.size();}

    public int getStopCount() {return legs.size() - 1;}

    public double getTotalMinutes() {
        
        double sum = 0;
        
        for (Route r : legs) sum += r.getMinutes();
        return sum;
    }

    public double getTotalCost() {
        
        double sum = 0;
        
        for (Route r : legs) sum += r.getCost();
        return sum;
    }
}
