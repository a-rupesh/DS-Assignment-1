package org.example.Calculator;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

// Testing calculator services using the client

public class CalculatorClient {
    public static void main(String[] args) {
        try {
            String clientId = UUID.randomUUID().toString(); // Unique ID created for the client
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            Calculator calc = (Calculator) registry.lookup("CalculatorService");
            calc.pushValue(clientId, 6);
            calc.pushValue(clientId, 18);
            calc.pushOperation(clientId, "gcd");
            System.out.println("GCD for client " + clientId + ": " + calc.pop(clientId));
            calc.pushValue(clientId,4);
            calc.pushValue(clientId,16);
            calc.pushOperation(clientId,"lcm");
            System.out.println("LCM for client " + clientId + ": " + calc.pop(clientId));

            calc.pushValue(clientId, 50);
            System.out.println("Delay Pop Result: " + calc.delayPop(clientId, 2000));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}