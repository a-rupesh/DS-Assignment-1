package org.example.Calculator;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

//Calculator RMI server starts here

public class CalculatorServer {
    public static void main(String[] args) {
        try {
            CalculatorImpl calc = new CalculatorImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("CalculatorService", calc);
            System.out.println("Calculator Server is running with each client stacks....");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
