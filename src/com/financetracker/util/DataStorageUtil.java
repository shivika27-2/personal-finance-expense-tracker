package com.financetracker.util;

import com.financetracker.model.Budget;
import com.financetracker.model.RecurringTransaction;
import com.financetracker.model.Transaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading application data (transactions, budgets, recurring
 * templates) to/from simple pipe-delimited flat files under a local "data" directory,
 * so the user's data persists across separate runs of the application.
 *
 * A lightweight pipe-delimited format is used instead of a full JSON library so the
 * application has zero external dependencies and compiles/runs with only the JDK.
 */
public class DataStorageUtil {

    private static final Path DATA_DIR = Path.of("data");
    private static final Path TRANSACTIONS_FILE = DATA_DIR.resolve("transactions.dat");
    private static final Path BUDGETS_FILE = DATA_DIR.resolve("budgets.dat");
    private static final Path RECURRING_FILE = DATA_DIR.resolve("recurring.dat");

    private void ensureDataDir() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
        }
    }

    // ---------------------------------------------------------------- transactions

    public void saveTransactions(List<Transaction> transactions) throws IOException {
        ensureDataDir();
        List<String> lines = new ArrayList<>();
        for (Transaction t : transactions) {
            lines.add(t.toStorageRow());
        }
        Files.write(TRANSACTIONS_FILE, lines, StandardCharsets.UTF_8);
    }

    public List<Transaction> loadTransactions() throws IOException {
        List<Transaction> result = new ArrayList<>();
        if (!Files.exists(TRANSACTIONS_FILE)) {
            return result;
        }
        List<String> lines = Files.readAllLines(TRANSACTIONS_FILE, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) continue;
            try {
                result.add(Transaction.fromStorageRow(line));
            } catch (Exception e) {
                System.out.println("Warning: skipping corrupted transaction record: " + line);
            }
        }
        return result;
    }

    // ---------------------------------------------------------------- budgets

    public void saveBudgets(List<Budget> budgets) throws IOException {
        ensureDataDir();
        List<String> lines = new ArrayList<>();
        for (Budget b : budgets) {
            lines.add(b.toStorageRow());
        }
        Files.write(BUDGETS_FILE, lines, StandardCharsets.UTF_8);
    }

    public List<Budget> loadBudgets() throws IOException {
        List<Budget> result = new ArrayList<>();
        if (!Files.exists(BUDGETS_FILE)) {
            return result;
        }
        List<String> lines = Files.readAllLines(BUDGETS_FILE, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) continue;
            try {
                result.add(Budget.fromStorageRow(line));
            } catch (Exception e) {
                System.out.println("Warning: skipping corrupted budget record: " + line);
            }
        }
        return result;
    }

    // ---------------------------------------------------------------- recurring transactions

    public void saveRecurring(List<RecurringTransaction> recurringList) throws IOException {
        ensureDataDir();
        List<String> lines = new ArrayList<>();
        for (RecurringTransaction r : recurringList) {
            lines.add(r.toStorageRow());
        }
        Files.write(RECURRING_FILE, lines, StandardCharsets.UTF_8);
    }

    public List<RecurringTransaction> loadRecurring() throws IOException {
        List<RecurringTransaction> result = new ArrayList<>();
        if (!Files.exists(RECURRING_FILE)) {
            return result;
        }
        List<String> lines = Files.readAllLines(RECURRING_FILE, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) continue;
            try {
                result.add(RecurringTransaction.fromStorageRow(line));
            } catch (Exception e) {
                System.out.println("Warning: skipping corrupted recurring record: " + line);
            }
        }
        return result;
    }

    /** Saves everything in one call; convenient for a clean shutdown / "save & exit" menu option. */
    public void saveAll(List<Transaction> transactions, List<Budget> budgets, List<RecurringTransaction> recurring) throws IOException {
        saveTransactions(transactions);
        saveBudgets(budgets);
        saveRecurring(recurring);
    }
}