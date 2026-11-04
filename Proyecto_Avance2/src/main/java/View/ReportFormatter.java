package View;

import Model.Airport;
import Model.CriticalRoute;
import Model.Graph;
import Model.Route;
import Model.RouteCriterion;
import Model.RoutePath;
import java.util.List;

public final class ReportFormatter {

    private ReportFormatter() { }

    public static String number(double v) {return v == Math.rint(v) ? String.valueOf((long) v) : String.format("%.1f", v);}

    public static String pathText(Graph graph, RoutePath path) {
        
        StringBuilder sb = new StringBuilder(graph.getAirport(path.getOrigin()).getCode());
        for (Route r : path.getLegs()) sb.append(" → ").append(graph.getAirport(r.getDestination()).getCode());
        return sb.toString();
    }

    public static String routeDetails(Graph graph, RoutePath path, RouteCriterion criterion) {
        
        StringBuilder sb = new StringBuilder();
        int legs = path.getFlightCount();
        double totalMinutes = path.getTotalMinutes();
        int tm = (int) Math.round(totalMinutes);
        
        sb.append("CRITERION\n").append(criterion.getLabel()).append("\n\n");
        sb.append("SEQUENCE\n").append(pathText(graph, path)).append("\n\n");
        sb.append("Flights: ").append(legs).append("\n");
        sb.append("Stops: ").append(path.getStopCount()).append("\n");
        sb.append(String.format("Total minutes: %s (%dh %02dm)\n", number(totalMinutes), tm / 60, tm % 60));
        sb.append("Total cost: $").append(number(path.getTotalCost())).append(" USD\n");
        sb.append("\nFLIGHT DETAILS\n");
        
        for (int k = 0; k < legs; k++) {
            
            Route r = path.getLegs().get(k);
            sb.append(String.format("· %d. %s → %s\n     %s min | $%s\n", k + 1,
                    graph.getAirport(r.getOrigin()).getCode(), graph.getAirport(r.getDestination()).getCode(),
                    number(r.getMinutes()), number(r.getCost())));
        }
        return sb.toString();
    }

    public static String airportInfo(Graph graph, int index) {
        
        Airport a = graph.getAirport(index);
        StringBuilder sb = new StringBuilder();
        
        sb.append("AIRPORT ").append(a.getCode()).append("\n");
        sb.append(a.getCity()).append(", ").append(a.getCountry()).append("\n");
        sb.append("Position: (").append(number(a.getX())).append(", ").append(number(a.getY())).append(")\n\n");
        
        List<Route> outgoing = graph.getOutgoingRoutes(index);
        sb.append("OUTGOING ROUTES (").append(outgoing.size()).append(")\n");
        
        if (outgoing.isEmpty()) sb.append("(no outgoing routes)\n");
        
        for (Route r : outgoing) {
            sb.append(String.format("%s → %s  %s min  $%s\n", a.getCode(),
                    graph.getAirport(r.getDestination()).getCode(), number(r.getMinutes()), number(r.getCost())));
        }
        return sb.toString();
    }

    public static String reachability(Graph graph, boolean[] reachable, int origin) {
        
        StringBuilder yes = new StringBuilder(), no = new StringBuilder();
        int ny = 0, nn = 0;
        
        for (int i = 0; i < graph.size(); i++) {
            
            if (i == origin) continue;
            
            String code = graph.getAirport(i).getCode();
            
            if (reachable[i]) { if (ny++ > 0) yes.append(", "); yes.append(code); }
            else { if (nn++ > 0) no.append(", "); no.append(code); }
        }
        return "REACHABLE FROM " + graph.getAirport(origin).getCode() + " (" + ny + ")\n" + (ny > 0 ? yes : "(none)")
                + "\n\nNOT REACHABLE (" + nn + ")\n" + (nn > 0 ? no : "(none)");
    }

    public static String criticalRoutes(Graph graph, int origin, List<CriticalRoute> critical) {
        
        StringBuilder sb = new StringBuilder("CRITICAL ROUTES FROM " + graph.getAirport(origin).getCode() + " (" + critical.size() + ")\n");
        sb.append("If any of them is cancelled, some airport stops being reachable.\n\n");
        
        for (CriticalRoute c : critical) {
            
            StringBuilder lost = new StringBuilder();
            
            for (int i : c.getLostAirports()) {
                
                if (lost.length() > 0) lost.append(", ");
                lost.append(graph.getAirport(i).getCode());
            }
            
            sb.append(graph.getAirport(c.getRoute().getOrigin()).getCode()).append(" → ")
              .append(graph.getAirport(c.getRoute().getDestination()).getCode())
              .append("  leaves without access to: ").append(lost).append("\n");
        }
        
        if (critical.isEmpty()) sb.append("(none)\n");
        return sb.toString();
    }
}
