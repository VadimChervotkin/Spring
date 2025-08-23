package org.example.bank.operations.processors;

import org.example.bank.operations.ConsoleOperationType;
import org.example.bank.operations.OperationCommandProcessor;
import org.example.bank.user.User;
import org.example.bank.user.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowAllUsersProcessor implements OperationCommandProcessor {

    private final UserService userService;

    public ShowAllUsersProcessor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void processOperation() {
        List<User> users = userService.getAllUsers();
        System.out.println("List of all users: ");
        users.forEach(System.out::println);
    }

    @Override
    public ConsoleOperationType getOperationType() {
        return ConsoleOperationType.SHOW_ALL_USERS;
    }
}
