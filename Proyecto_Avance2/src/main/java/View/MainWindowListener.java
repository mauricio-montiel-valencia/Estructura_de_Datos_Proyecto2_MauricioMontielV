package View;

public interface MainWindowListener {

    void onSearchRoute();
    void onCriterionChanged();
    void onReachableAirports();
    void onAirportInfo();
    void onCancelRoute();
    void onRestoreRoutes();
    void onCriticalRoutes();
    void onLoadFiles();
    void onAirportClicked(int airportIndex);
}