package Model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class ReachabilityAnalyzer {

    private final Graph graph;

    public ReachabilityAnalyzer(Graph graph) {this.graph = graph;}

    public boolean[] reachableFrom(int origin) {return reachableFrom(origin, null);}

    public boolean[] reachableFrom(int origin, Route ignored) {
        
        int n = graph.size();
        boolean[] visited = new boolean[n];
        int[] next = new int[n];                     
        ArrayDeque<Integer> stack = new ArrayDeque<>();
        visited[origin] = true;
        stack.push(origin);
        
        while (!stack.isEmpty()) {
            
            int u = stack.peek();
            List<Route> outgoing = graph.getOutgoingRoutes(u);
            
            if (next[u] < outgoing.size()) {
                
                Route r = outgoing.get(next[u]++);
                
                if (r == ignored) continue;
                int v = r.getDestination();
                
                if (!visited[v]) {
                    
                    visited[v] = true;
                    stack.push(v);
                }
            } else {stack.pop();}
        }
        return visited;
    }

    public List<CriticalRoute> findCriticalRoutes(int origin) {
        
        boolean[] base = reachableFrom(origin);
        List<CriticalRoute> critical = new ArrayList<>();
        
        for (int u = 0; u < graph.size(); u++) {
            
            if (!base[u]) continue;
            
            for (Route r : graph.getOutgoingRoutes(u)) {
                
                boolean[] without = reachableFrom(origin, r);
                List<Integer> lost = new ArrayList<>();
                
                for (int i = 0; i < graph.size(); i++) {if (base[i] && !without[i]) lost.add(i);}
                
                if (!lost.isEmpty()) critical.add(new CriticalRoute(r, lost));
            }
        }
        return critical;
    }
}
