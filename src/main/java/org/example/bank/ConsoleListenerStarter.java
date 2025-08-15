package org.example.bank;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class ConsoleListenerStarter {

    private final OperationConsoleListener consoleListener;
    private Thread consoleListenerThread;

    public ConsoleListenerStarter(OperationConsoleListener operationConsoleListener) {
        this.consoleListener = operationConsoleListener;
    }

    @PostConstruct
    public void postConstruct() {
        consoleListenerThread = new Thread(() -> {
            consoleListener.start();
            consoleListener.listenUpdates();
        });
        consoleListenerThread.start();
    }

    @PreDestroy
    public void preDestroy() {
        consoleListenerThread.interrupt();
        consoleListener.endListen();
    }
}

