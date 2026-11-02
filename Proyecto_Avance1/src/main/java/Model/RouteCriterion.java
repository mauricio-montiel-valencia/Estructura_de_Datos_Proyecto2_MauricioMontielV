package Model;

public enum RouteCriterion {

    FEWEST_STOPS("Fewest stops (BFS)"),
    FASTEST("Fastest (Dijkstra)"),
    CHEAPEST("Cheapest (Dijkstra)");

    private final String label;

    RouteCriterion(String label) {this.label = label;}

    public String getLabel() {return label;}
}