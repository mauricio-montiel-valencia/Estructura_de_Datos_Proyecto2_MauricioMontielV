package Model;

public class Route {

    private final int origin;
    private final int destination;
    private final double minutes;
    private final double cost;

    public Route(int origin, int destination, double minutes, double cost) {
        
        this.origin = origin;
        this.destination = destination;
        this.minutes = minutes;
        this.cost = cost;
    }

    public int getOrigin() {return origin;}
    public int getDestination() {return destination;}
    public double getMinutes() {return minutes;}
    public double getCost() {return cost;}
}
