package Model;

import java.util.Collections;
import java.util.List;

public class CriticalRoute {

    private final Route route;
    private final List<Integer> lostAirports;

    public CriticalRoute(Route route, List<Integer> lostAirports) {
        
        this.route = route;
        this.lostAirports = Collections.unmodifiableList(lostAirports);
    }

    public Route getRoute() {return route;}
    
    public List<Integer> getLostAirports() {return lostAirports;}
}
