package com.financetracker.model;

import com.financetracker.exception.InvalidTransactionException;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the running balance and full transaction history for the user.
 */
public class Account {

    private final String ownerName;
    private final List<Transaction> transactions = new ArrayList<>();
    private double balance;

    public Account(String ownerName) {
        this.ownerName = ownerName;
        this.balance = 0.0;
    }

    public Account(String ownerName, double startingBalance) {
        this.ownerName = ownerName;
        this.balance = startingBalance;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * Validates and records a transaction, adjusting the running balance.
     *
     * @throws InvalidTransactionException if the transaction fails validation rules
     */
    public void addTransaction(Transaction transaction) throws InvalidTransactionException {
        validate(transaction);
        transactions.add(transaction);
        if (transaction.getType() == Transaction.Type.INCOME) {
            balance += transaction.getAmount();
        } else {
            balance -= transaction.getAmount();
        }
    }

    /**
     * Loads a transaction that was already validated previously (e.g. from disk)
     * without re-running validation, but still updates the balance.
     */
    public void loadTransaction(Transaction transaction) {
        transactions.add(transaction);
        if (transaction.getType() == Transaction.Type.INCOME) {
            balance += transaction.getAmount();
        } else {
            balance -= transaction.getAmount();
        }
    }

    public boolean removeTransaction(int id) {
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            if (t.getId() == id) {
                transactions.remove(i);
                if (t.getType() == Transaction.Type.INCOME) {
                    balance -= t.getAmount();
                } else {
                    balance += t.getAmount();
                }
                return true;
            }
        }
        return false;
    }

    private void validate(Transaction transaction) throws InvalidTransactionException {
        if (transaction == null) {
            throw new InvalidTransactionException("Transaction cannot be null.");
        }
        if (transaction.getAmount() <= 0) {
            throw new InvalidTransactionException("Transaction amount must be greater than zero. Got: " + transaction.getAmount());
        }
        if (transaction.getCategory() == null) {
            throw new InvalidTransactionException("Transaction category must not be null.");
        }
        if (transaction.getDate() == null) {
            throw new InvalidTransactionException("Transaction date must not be null.");
        }
        if (transaction.getType() == Transaction.Type.EXPENSE && transaction.getAmount() > 1_000_000_000.0) {
            throw new InvalidTransactionException("Transaction amount is unrealistically large.");
        }
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public List<Transaction> getTransactionsForMonth(YearMonth ym) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (YearMonth.from(t.getDate()).equals(ym)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Transaction> getTransactionsByCategory(Category category) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getCategory() == category) {
                result.add(t);
            }
        }
        return result;
    }

    /** Total amount spent (EXPENSE only) in a given category during a given month. */
    public double getTotalSpentForCategory(Category category, YearMonth ym) {
        double total = 0.0;
        for (Transaction t : transactions) {
            if (t.getType() == Transaction.Type.EXPENSE
                    && t.getCategory() == category
                    && YearMonth.from(t.getDate()).equals(ym)) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpense() {
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalIncomeForMonth(YearMonth ym) {
        return getTransactionsForMonth(ym).stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenseForMonth(YearMonth ym) {
        return getTransactionsForMonth(ym).stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}