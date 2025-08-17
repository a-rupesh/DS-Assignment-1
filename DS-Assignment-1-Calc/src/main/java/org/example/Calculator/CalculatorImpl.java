package org.example.Calculator;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class CalculatorImpl extends UnicastRemoteObject implements Calculator {

    // For each client, separate clintID created
    private final Map<String, Stack<Integer>> clientStacks;

    public CalculatorImpl() throws RemoteException {
        clientStacks = new HashMap<>();
    }

    // Create a stack for a client
    private synchronized Stack<Integer> getStack(String clientId) {
        return clientStacks.computeIfAbsent(clientId, k -> new Stack<>());
    }

    @Override
    public synchronized void pushValue(String clientId, int val) throws RemoteException {
        getStack(clientId).push(val);
    }

    // Performing stack operations
    @Override
    public synchronized void pushOperation(String clientId, String operator) throws RemoteException {
        Stack<Integer> stack = getStack(clientId);
        if (stack.isEmpty()) return;

        int result = stack.pop();
        while (!stack.isEmpty()) {
            int value = stack.pop();
            switch (operator.toLowerCase()) {
                case "min":
                    result = Math.min(result, value);
                    break;
                case "max":
                    result = Math.max(result, value);
                    break;
                case "gcd":
                    result = gcdOfNums(result, value);
                    break;
                case "lcm":
                    result = lcmOfNums(result, value);
                    break;
            }
        }
        stack.push(result);
    }

    // Pops the final value in stack
    @Override
    public synchronized int pop(String clientId) throws RemoteException {
        return getStack(clientId).pop();
    }

    @Override
    public synchronized boolean isEmpty(String clientId) throws RemoteException {
        return getStack(clientId).isEmpty();
    }

    //time delay before popping(ref: baeldung-https://www.baeldung.com/mockito-delay-stubbed-method-response)
    @Override
    public synchronized int delayPop(String clientId, int millis) throws RemoteException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return pop(clientId);
    }

    // gcd calculation(ref: geekforgeeks-https://www.geeksforgeeks.org/java/java-program-to-compute-gcd/)
    private int gcdOfNums(int num1, int num2) {
        if (num2 == 0)
            return num1;
        else
            return gcdOfNums(num2, Math.abs(num1 - num2));
    }

    // lcm calculation(ref: geeksforgeeks-https://www.geeksforgeeks.org/java/java-program-to-find-lcm-of-two-numbers/)
    private int lcmOfNums(int num1, int num2) {
        return (num1 / gcdOfNums(num1, num2)) * num2;
    }
}
