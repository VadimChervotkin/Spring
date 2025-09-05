package org.example.bank.account;

import org.example.bank.hibernate.TransactionHelper;
import org.example.bank.user.User;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;

@Service
public class AccountService {

    private final SessionFactory sessionFactory;
    private final AccountProperties accountProperties;
    private final TransactionHelper transactionHelper;

    public AccountService(SessionFactory sessionFactory, AccountProperties accountProperties, TransactionHelper transactionHelper) {
        this.sessionFactory = sessionFactory;
        this.accountProperties = accountProperties;
        this.transactionHelper = transactionHelper;
    }

    public Account createAccount(User user) {
        return transactionHelper.executeInTransaction(() -> {
            Account newAccount = new Account(user, accountProperties.getDefaultAccountAmount());
            sessionFactory.getCurrentSession().persist(newAccount);
            return newAccount;
        });
    }

    private Optional<Account> findAccountById(Long id) {
        var account = sessionFactory.getCurrentSession()
                .get(Account.class, id);
        return Optional.of(account);
    }


    public void depositAccount(Long accountId, int moneyToDeposit) {
        transactionHelper.executeInTransaction(() -> {
            var account = findAccountById(accountId)
                    .orElseThrow(() -> new IllegalStateException("Account with id " + accountId + " not found"));
            if (moneyToDeposit <= 0) {
                throw new IllegalArgumentException("Money to deposit must be greater than zero");
            }
            account.setMoneyAmount(account.getMoneyAmount() + moneyToDeposit);
            return 0;
        });
    }


    public void withdrawFromAccount(Long accountId, int amountToWithdraw) {
        transactionHelper.executeInTransaction(() -> {
            var account = findAccountById(accountId)
                    .orElseThrow(() -> new IllegalStateException("Account with id " + accountId + " not found"));
            if (amountToWithdraw <= 0) {
                throw new IllegalArgumentException("Amount to withdraw must be greater than zero");
            }
            if (account.getMoneyAmount() < amountToWithdraw) {
                throw new IllegalArgumentException("Not enough money to withdraw");
            }
            account.setMoneyAmount(account.getMoneyAmount() - amountToWithdraw);
            return 0;
        });
    }

    public void transfer(Long fromAccountId, Long toAccountId, int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");
        transactionHelper.executeInTransaction(()-> {
                var from = findAccountById(fromAccountId)
                        .orElseThrow(() -> new IllegalStateException("Account with id " + fromAccountId + " not found"));
                var to = findAccountById(toAccountId)
                        .orElseThrow(() -> new IllegalStateException("Account with id " + toAccountId + " not found"));

                if (from.getMoneyAmount() < amount)
                    throw new IllegalArgumentException("Not enough money to transfer");

                int totalDeposit = !to.getUser().getId().equals(from.getUser().getId())
                        ? (int) (amount * (1 - accountProperties.getTransferCommission()))
                        : amount;

                from.setMoneyAmount(from.getMoneyAmount() - amount);
                to.setMoneyAmount(to.getMoneyAmount() + totalDeposit);
                return 0;
        });
    }

    public Account closeAccount(Long accountId) {
        return transactionHelper.executeInTransaction(() -> {
            var accountToRemove = findAccountById(accountId)
                    .orElseThrow(() -> new IllegalStateException("Account with id " + accountId + " not found"));

            var accounts = accountToRemove.getUser().getAccountList();
            if (accounts.size() <= 1)
                throw new IllegalArgumentException("Cannot close the only account");

            Account accountToDeposit = accounts.stream()
                    .filter(a -> !Objects.equals(a.getId(), accountId))
                    .findFirst()
                    .orElseThrow();

            accountToDeposit.setMoneyAmount(accountToDeposit.getMoneyAmount() + accountToRemove.getMoneyAmount());


            sessionFactory.getCurrentSession().remove(accountToRemove);


            return accountToRemove;
        });

    }
}
