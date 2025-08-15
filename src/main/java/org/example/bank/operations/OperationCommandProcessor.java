package org.example.bank.operations;

public interface OperationCommandProcessor {

    void processOperation();
    ConsoleOperationType getOperationType();
}
