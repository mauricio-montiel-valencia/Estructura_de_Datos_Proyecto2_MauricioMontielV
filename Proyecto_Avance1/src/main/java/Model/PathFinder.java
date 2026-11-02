package Model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.ToDoubleFunction;

public class PathFinder {

    private final Graph graph;

    public PathFinder(Graph graph) {
        this.graph = graph;
    }

    public RoutePath find(int origin, int destination, RouteCriterion criterion) {
        
        switch (criterion) {
            
            case FEWEST_STOPS:return bfs(origin, destination);
            case FASTEST:return dijkstra(origin, destination, Route::getMinutes);
            default:return dijkstra(origin, destination, Route::getCost);
        }
    }

    private RoutePath rebuild(Route[] previous, int origin, int destination) {
        
        List<Route> legs = new ArrayList<>();
        int v = destination;
        
        while (v != origin) {Route r = previous[v]; legs.add(r); v = r.getOrigin();}
        Collections.reverse(legs);
        return new RoutePath(legs);
    }

    public RoutePath bfs(int origin, int destination) {
        
        int n = graph.size();
        Route[] previous = new Route[n];
        boolean[] visited = new boolean[n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        visited[origin] = true;
        queue.add(origin);
        
        while (!queue.isEmpty()) {
            
            int u = queue.poll();
            if (u == destination) break;
            
            for (Route r : graph.getOutgoingRoutes(u)) {
                
                int v = r.getDestination();
                
                if (!visited[v]) {
                    
                    visited[v] = true;
                    previous[v] = r;
                    queue.add(v);
                }
            }
        }
        if (!visited[destination]) return null;
        return rebuild(previous, origin, destination);
    }

    public RoutePath dijkstra(int origin, int destination, ToDoubleFunction<Route> weight) {
        
        int n = graph.size();
        double[] dist = new double[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Route[] previous = new Route[n];
        PriorityQueue<double[]> queue = new PriorityQueue<>((p, q) -> Double.compare(p[0], q[0]));  
        dist[origin] = 0;
        queue.add(new double[]{0, origin});
        
        while (!queue.isEmpty()) {
            
            double[] top = queue.poll();
            int u = (int) top[1];
            
            if (top[0] > dist[u]) continue;           
            if (u == destination) break;
            
            for (Route r : graph.getOutgoingRoutes(u)) {
                
                int v = r.getDestination();
                double nd = dist[u] + weight.applyAsDouble(r);
                
                if (nd < dist[v]) {
                    
                    dist[v] = nd;
                    previous[v] = r;
                    queue.add(new double[]{nd, v});
                }
            }
        }
        if (dist[destination] == Double.POSITIVE_INFINITY) return null;
        return rebuild(previous, origin, destination);
    }
}