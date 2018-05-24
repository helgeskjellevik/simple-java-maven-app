package com.mycompany.app;

/**
 * Hello world!
 */
public class App
{

    private final String message = "Hello World!";

    public App() {}

    public static void main(String[] args) {
        System.out.println(new App().getMessage());
    }

    private final String getMessage() {

        String m = "a";
        m = "b";
        m = "c";

        String n = "h";

        if(m == m) {
            System.out.println("oh no");
        }


        return message;
    }

}
