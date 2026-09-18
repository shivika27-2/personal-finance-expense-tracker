package com.financetracker.main;

import com.financetracker.exception.BudgetExceededException;
import com.financetracker.exception.InvalidTransactionException;
import com.financetracker.model.Account;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.RecurringTransaction;
import com.financetracker.model.Transaction;
import com.financetracker.service.AlertEngine;
import com.financetracker.service.ReportGenerator;
import com.financetracker.util.DataStorageUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line entry point for the Personal Finance & Expense Tracker.
 * Presents an interactive menu, wires together the model/service/util layers,
 * and persists data between runs via {@link DataStorageUtil}.
 */
public class MainApp {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final Scanner scanner = new Scanner(System.in);
    private final DataStorageUtil storage = new DataStorageUtil();
    private final AlertEngine alertEngine = new AlertEngine();
    private final ReportGenerator reportGenerator = new ReportGenerator();

    private Account account;
    private final List<Budget> budgets = new ArrayList<>();
    private final List<RecurringTransaction> recurringTransactions = new ArrayList<>();

    public static void main(String[] args) {
        MainApp app = new MainApp();
        app.run();
    }

    private void run() {
        System.out.println("=================================================================");
        System.out.println("     PERSONAL FINANCE & EXPENSE TRACKER  -  Budget Alert Edition");
        System.out.println("=================================================================");
        loadAllData();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = promptLine("Choose an option: ").trim();
            try {
                switch (choice) {
                    case "1" -> addTransaction();
                    case "2" -> viewAllTransactions();
                    case "3" -> viewTransactionsByCategory();
                    case "4" -> setOrUpdateBudget();
                    case "5" -> viewBudgetsAndAlerts();
                    case "6" -> addRecurringTransaction();
                    case "7" -> viewRecurringTransactions();
                    case "8" -> processDueRecurringTransactions();
                    case "9" -> generateSummaryReportToConsole();
                    case "10" -> exportTextReport();
                    case "11" -> exportTransactionsCsv();
                    case "12" -> exportCategorySummaryCsv();
                    case "13" -> deleteTransaction();
                    case "14" -> {
                        saveAllData();
                        System.out.println("Data saved. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("Invalid option. Please choose a number from the menu.");
                }
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
            System.out.println();
        }
        scanner.close();
    }

    private void printMenu() {
        System.out.println("-----------------------------------------------------------------");
        System.out.printf("Account: %s | Balance: %.2f%n", account.getOwnerName(), account.getBalance());
        System.out.println("-----------------------------------------------------------------");
        System.out.println(" 1. Add Transaction (Income/Expense)");
        System.out.println(" 2. View All Transactions");
        System.out.println(" 3. View Transactions by Category");
        System.out.println(" 4. Set / Update Monthly Budget");
        System.out.println(" 5. View Budgets & Alerts (this month)");
        System.out.println(" 6. Add Recurring Transaction");
        System.out.println(" 7. View Recurring Transactions");
        System.out.println(" 8. Process Due Recurring Transactions");
        System.out.println(" 9. Generate Summary Report (view on screen)");
        System.out.println("10. Export Summary Report to Text File");
        System.out.println("11. Export Transactions to CSV");
        System.out.println("12. Export Category Summary to CSV");
        System.out.println("13. Delete a Transaction");
        System.out.println("14. Save & Exit");
    }

    // ------------------------------------------------------------ setup / persistence

    private void loadAllData() {
        String owner = promptLine("Enter account owner name: ").trim();
        if (owner.isEmpty()) {
            owner = "User";
        }
        account = new Account(owner);
        try {
            List<Transaction> loadedTx = storage.loadTransactions();
            for (Transaction t : loadedTx) {
                account.loadTransaction(t);
            }
            budgets.addAll(storage.loadBudgets());
            recurringTransactions.addAll(storage.loadRecurring());
            System.out.printf("Loaded %d transaction(s), %d budget(s), %d recurring template(s) from disk.%n",
                    loadedTx.size(), budgets.size(), recurringTransactions.size());
        } catch (IOException e) {
            System.out.println("Warning: could not load existing data (" + e.getMessage() + "). Starting fresh.");
        }
    }

    private void saveAllData() {
        try {
            storage.saveAll(account.getAllTransactions(), budgets, recurringTransactions);
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ transactions

    private void addTransaction() {
        System.out.println("Transaction Type: 1) INCOME  2) EXPENSE");
        String typeChoice = promptLine("Select type: ").trim();
        Transaction.Type type;
        if (typeChoice.equals("1")) {
            type = Transaction.Type.INCOME;
        } else if (typeChoice.equals("2")) {
            type = Transaction.Type.EXPENSE;
        } else {
            System.out.println("Invalid type selection. Cancelled.");
            return;
        }

        double amount = promptDouble("Enter amount: ");
        Category category = promptCategory();
        if (category == null) {
            System.out.println("Cancelled.");
            return;
        }
        LocalDate date = promptDateOrToday("Enter date (yyyy-MM-dd) or press Enter for today: ");
        String description = promptLine("Enter description (optional): ");

        Transaction transaction = new Transaction(type, amount, category, date, description);
        try {
            account.addTransaction(transaction);
            System.out.println("Transaction recorded: " + transaction);
        } catch (InvalidTransactionException e) {
            System.out.println("Could not add transaction: " + e.getMessage());
            return;
        }

        if (type == Transaction.Type.EXPENSE) {
            YearMonth month = YearMonth.from(date);
            Budget budget = alertEngine.findBudget(budgets, category, month);
            if (budget != null) {
                try {
                    alertEngine.evaluate(account, budget);
                } catch (BudgetExceededException e) {
                    System.out.println("(Transaction was still recorded, but budget is now over its limit.)");
                }
            }
        }
    }

    private void viewAllTransactions() {
        List<Transaction> all = account.getAllTransactions();
        if (all.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        System.out.println("All Transactions:");
        for (Transaction t : all) {
            System.out.println("  " + t);
        }
        System.out.printf("Total Income: %.2f | Total Expense: %.2f | Balance: %.2f%n",
                account.getTotalIncome(), account.getTotalExpense(), account.getBalance());
    }

    private void viewTransactionsByCategory() {
        Category category = promptCategory();
        if (category == null) return;
        List<Transaction> list = account.getTransactionsByCategory(category);
        if (list.isEmpty()) {
            System.out.println("No transactions found for category " + category);
            return;
        }
        System.out.println("Transactions for " + category + ":");
        for (Transaction t : list) {
            System.out.println("  " + t);
        }
    }

    private void deleteTransaction() {
        int id = (int) promptDouble("Enter the ID of the transaction to delete: ");
        boolean removed = account.removeTransaction(id);
        System.out.println(removed ? "Transaction #" + id + " removed." : "No transaction found with ID " + id);
    }

    // ------------------------------------------------------------ budgets

    private void setOrUpdateBudget() {
        Category category = promptCategory();
        if (category == null) return;
        YearMonth month = promptYearMonthOrCurrent();
        double limit = promptDouble("Enter monthly budget limit for " + category + " in " + month + ": ");

        Budget existing = alertEngine.findBudget(budgets, category, month);
        if (existing != null) {
            existing.setLimitAmount(limit);
            System.out.println("Updated existing budget: " + existing);
        } else {
            Budget budget = new Budget(category, limit, month);
            budgets.add(budget);
            System.out.println("Budget created: " + budget);
        }
    }

    private void viewBudgetsAndAlerts() {
        YearMonth month = promptYearMonthOrCurrent();
        List<AlertEngine.AlertResult> results = alertEngine.checkAllBudgets(account, budgets, month);
        if (results.isEmpty()) {
            System.out.println("No budgets configured for " + month + ".");
            return;
        }
        System.out.println("Budget status for " + month + ":");
        for (AlertEngine.AlertResult r : results) {
            System.out.println("  " + r);
        }
    }

    // ------------------------------------------------------------ recurring transactions

    private void addRecurringTransaction() {
        System.out.println("Recurring Transaction Type: 1) INCOME  2) EXPENSE");
        String typeChoice = promptLine("Select type: ").trim();
        Transaction.Type type;
        if (typeChoice.equals("1")) {
            type = Transaction.Type.INCOME;
        } else if (typeChoice.equals("2")) {
            type = Transaction.Type.EXPENSE;
        } else {
            System.out.println("Invalid type selection. Cancelled.");
            return;
        }
        double amount = promptDouble("Enter amount: ");
        Category category = promptCategory();
        if (category == null) return;
        String description = promptLine("Enter description (optional): ");

        System.out.println("Frequency: 1) DAILY  2) WEEKLY  3) MONTHLY  4) YEARLY");
        String freqChoice = promptLine("Select frequency: ").trim();
        RecurringTransaction.Frequency frequency = switch (freqChoice) {
            case "1" -> RecurringTransaction.Frequency.DAILY;
            case "2" -> RecurringTransaction.Frequency.WEEKLY;
            case "3" -> RecurringTransaction.Frequency.MONTHLY;
            case "4" -> RecurringTransaction.Frequency.YEARLY;
            default -> null;
        };
        if (frequency == null) {
            System.out.println("Invalid frequency selection. Cancelled.");
            return;
        }
        LocalDate startDate = promptDateOrToday("Enter first due date (yyyy-MM-dd) or press Enter for today: ");

        RecurringTransaction recurring = new RecurringTransaction(type, amount, category, description, frequency, startDate);
        recurringTransactions.add(recurring);
        System.out.println("Recurring transaction scheduled: " + recurring);
    }

    private void viewRecurringTransactions() {
        if (recurringTransactions.isEmpty()) {
            System.out.println("No recurring transactions configured.");
            return;
        }
        for (RecurringTransaction r : recurringTransactions) {
            System.out.println("  " + r);
        }
    }

    private void processDueRecurringTransactions() {
        LocalDate asOf = promptDateOrToday("Process as of date (yyyy-MM-dd) or press Enter for today: ");
        int generated = 0;
        for (RecurringTransaction r : recurringTransactions) {
            while (r.isDue(asOf)) {
                Transaction t = r.materialize();
                try {
                    account.addTransaction(t);
                    System.out.println("Generated: " + t);
                    generated++;
                    if (t.getType() == Transaction.Type.EXPENSE) {
                        YearMonth month = YearMonth.from(t.getDate());
                        Budget budget = alertEngine.findBudget(budgets, t.getCategory(), month);
                        if (budget != null) {
                            try {
                                alertEngine.evaluate(account, budget);
                            } catch (BudgetExceededException e) {
                                // Already printed by the alert engine; transaction stays recorded.
                            }
                        }
                    }
                } catch (InvalidTransactionException e) {
                    System.out.println("Skipped invalid recurring instance: " + e.getMessage());
                }
            }
        }
        System.out.println(generated == 0
                ? "No recurring transactions were due as of " + asOf + "."
                : generated + " recurring transaction(s) generated.");
    }

    // ------------------------------------------------------------ reports

    private void generateSummaryReportToConsole() {
        YearMonth month = promptYearMonthOrCurrent();
        String report = reportGenerator.generateSummaryReport(account, budgets, month);
        System.out.println(report);
    }

    private void exportTextReport() {
        YearMonth month = promptYearMonthOrCurrent();
        String report = reportGenerator.generateSummaryReport(account, budgets, month);
        String path = promptLine("Enter output file path (default: data/report_" + month + ".txt): ").trim();
        if (path.isEmpty()) {
            path = "data/report_" + month + ".txt";
        }
        try {
            reportGenerator.exportTextReport(report, path);
            System.out.println("Report exported to " + path);
        } catch (IOException e) {
            System.out.println("Failed to export report: " + e.getMessage());
        }
    }

    private void exportTransactionsCsv() {
        String path = promptLine("Enter output CSV path (default: data/transactions_export.csv): ").trim();
        if (path.isEmpty()) {
            path = "data/transactions_export.csv";
        }
        try {
            reportGenerator.exportTransactionsCsv(account, path);
            System.out.println("Transactions exported to " + path);
        } catch (IOException e) {
            System.out.println("Failed to export CSV: " + e.getMessage());
        }
    }

    private void exportCategorySummaryCsv() {
        YearMonth month = promptYearMonthOrCurrent();
        String path = promptLine("Enter output CSV path (default: data/category_summary_" + month + ".csv): ").trim();
        if (path.isEmpty()) {
            path = "data/category_summary_" + month + ".csv";
        }
        try {
            reportGenerator.exportCategorySummaryCsv(account, month, path);
            System.out.println("Category summary exported to " + path);
        } catch (IOException e) {
            System.out.println("Failed to export CSV: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ input helpers

    private String promptLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private double promptDouble(String prompt) {
        while (true) {
            String input = promptLine(prompt).trim();
            try {
                double value = Double.parseDouble(input);
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private Category promptCategory() {
        System.out.println("Categories:");
        Category.printAll();
        String input = promptLine("Enter category name or number (blank to cancel): ").trim();
        if (input.isEmpty()) {
            return null;
        }
        Category[] values = Category.values();
        try {
            int idx = Integer.parseInt(input);
            if (idx >= 1 && idx <= values.length) {
                return values[idx - 1];
            }
            System.out.println("Number out of range.");
            return promptCategory();
        } catch (NumberFormatException ignored) {
            // not a number, fall through to name lookup
        }
        try {
            return Category.fromString(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Unrecognized category. Try again.");
            return promptCategory();
        }
    }

    private LocalDate promptDateOrToday(String prompt) {
        while (true) {
            String input = promptLine(prompt).trim();
            if (input.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }

    private YearMonth promptYearMonthOrCurrent() {
        while (true) {
            String input = promptLine("Enter month (yyyy-MM) or press Enter for current month: ").trim();
            if (input.isEmpty()) {
                return YearMonth.now();
            }
            try {
                return YearMonth.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Please use yyyy-MM (e.g. 2026-09).");
            }
        }
    }
}
