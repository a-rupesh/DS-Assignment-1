package org.example.Calculator;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

// multiple clients interacting with the server

public class CalculatorMultiClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            Calculator calc = (Calculator) registry.lookup("CalculatorService");

            // multiple clients created using threads
            for (int i = 1; i <= 4; i++) {
                final int clientNumber = i;
                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String clientId = "Client" + clientNumber + "-" + UUID.randomUUID();
                            calc.pushValue(clientId, 10);
                            calc.pushValue(clientId, 20);
                            calc.pushOperation(clientId, "max");
                            int result = calc.pop(clientId);
                            System.out.println("Result from " + clientId + ": " + result);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                clientThread.start();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
