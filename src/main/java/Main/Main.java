package Main;

import Controller.AppController;
import Model.GraphLoader;
import View.ConsoleView;

public class Main {

    public static void main(String[] args) {new AppController(new GraphLoader(), new ConsoleView()).run();}
}