package org.example.bank.user;

import org.example.bank.account.AccountService;
import org.example.bank.hibernate.TransactionHelper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final SessionFactory sessionFactory;
    private final AccountService accountService;
    private final TransactionHelper transactionHelper;

    public UserService(SessionFactory sessionFactory, AccountService accountService, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.accountService = accountService;
        this.transactionHelper = transactionHelper;
    }

    public User createUser(String login) {
        return transactionHelper.executeInTransaction(() -> {
            var session = sessionFactory.getCurrentSession();
            var existing = session.createQuery("FROM User u WHERE u.login = :login", User.class)
                    .setParameter("login", login)
                    .getSingleResultOrNull();
            if (existing != null) {
                throw new IllegalArgumentException("User already exists with login: " + login);
            }

            User newUser = new User(login, new ArrayList<>());
            session.save(newUser);

            accountService.createAccount(newUser);
            return newUser;
        });
    }

    public Optional<User> findUserById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            var user = session.get(User.class, id);
            return Optional.of(user);
        }
    }

    public List<User> getAllUsers() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("SELECT u FROM User u LEFT JOIN FETCH u.accountList", User.class).list();

        }
    }
}
