package org.example.bank;


import org.example.bank.operations.ConsoleOperationType;
import org.example.bank.operations.OperationCommandProcessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@Component
public class OperationConsoleListener {

    private final Scanner scanner;
    private final Map<ConsoleOperationType, OperationCommandProcessor> processorMap;

    public OperationConsoleListener(
            Scanner scanner,
            List<OperationCommandProcessor> processorList) {
        this.scanner = scanner;
        this.processorMap = processorList
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        OperationCommandProcessor::getOperationType,
                                        processor -> processor
                                )
                        );;
    }

    public void listenUpdates() {
        while (!Thread.currentThread().isInterrupted()) {
            var operationType = listenNextOperation();
            if (operationType == null) {
                return;
            }
            processNextOperation(operationType);
        }
    }

    private ConsoleOperationType listenNextOperation() {
        System.out.println("\nPlease type next operation: ");
        printAllAvailableOperations();
        System.out.println();
        while (!Thread.currentThread().isInterrupted()) {
            var nextOperation = scanner.nextLine();
            try {
                return ConsoleOperationType.valueOf(nextOperation);
            } catch (IllegalArgumentException e) {
                System.out.println("No such command found");
            }
        }
        return null;
    }

    private void printAllAvailableOperations() {
        processorMap.keySet()
                .forEach(System.out::println);
    }

    private void processNextOperation(ConsoleOperationType operation) {
        try {
            var processor = processorMap.get(operation);
            processor.processOperation();
        } catch (Exception e) {
            System.out.println("Error executing command: " + operation + ": " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("Console Listener started");
    }

    public void endListen() {
        System.out.println("Console Listener end listen");
    }
}
