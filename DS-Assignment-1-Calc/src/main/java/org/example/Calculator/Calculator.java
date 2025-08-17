package org.example.Calculator;

import java.rmi.Remote;
import java.rmi.RemoteException;

// Importing remote interface for calculator services

public interface Calculator extends Remote {

    // To push an integer value in a specific client's stack.
    void pushValue(String clientId, int val) throws RemoteException;

    // To apply an operation "min" or "max" or "gcd" or "lcm" across the client's stack.
    void pushOperation(String clientId, String operator) throws RemoteException;

    // To pop and return the top value from the stack.
    int pop(String clientId) throws RemoteException;

    // To check if the stack is empty or not.
    boolean isEmpty(String clientId) throws RemoteException;

    // To wait millisecond, then pop from the client's stack to return the value.
    int delayPop(String clientId, int millis) throws RemoteException;
}
