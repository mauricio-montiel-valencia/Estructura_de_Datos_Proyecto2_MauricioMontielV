package Model;

public class Airport {

    private final String code;
    private final String city;
    private final String country;
    private final double x;
    private final double y;

    public Airport(String code, String city, String country, double x, double y) {
        
        this.code = code;
        this.city = city;
        this.country = country;
        this.x = x;
        this.y = y;
    }

    public String getCode() {return code;}
    public String getCity() {return city;}
    public String getCountry() {return country;}
    public double getX() {return x;}
    public double getY() {return y;}
}
