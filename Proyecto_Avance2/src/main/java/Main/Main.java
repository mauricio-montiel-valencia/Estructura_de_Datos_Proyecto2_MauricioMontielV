package Main;

import Controller.AppController;
import Model.GraphLoader;
import View.MainWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        
        try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
        catch (Exception ex) {}
        
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            new AppController(new GraphLoader(), window).start();
            window.setVisible(true);
        });
    }
}