package org.example;
import jexer.TAction;
import jexer.TApplication;
import jexer.TWindow;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends TApplication {
    public Main() throws Exception{
        super(BackendType.SWING);
//        TWindow window = addWindow("Hello World", 600, 600);
//        window.addLabel("Hello Jexer", 2, 2);
//        window.addButton("Exit", 2, 4, new TAction() {
//            @Override
//            public void DO() {
//                getActiveWindow().close();
//            }
//        });
//        window.addCalendar(3,5, new TAction() {
//            @Override
//            public void DO() {
//
//            }
//        });
//        addToolMenu();
//        addFileMenu();
//        addWindowMenu();
//        addHelpMenu();
    }
    public static void main(String[] args) throws Exception {
        //Main app = new Main();
        //app.run();
        studentLogin login = new studentLogin();
        //System.out.println("Hello World");
    }
}