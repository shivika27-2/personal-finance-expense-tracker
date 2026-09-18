package com.financetracker.service;

import com.financetracker.model.Account;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;
import com.financetracker.model.Transaction;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Builds human-readable summary reports and exports data to CSV/plain text files.
 */
public class ReportGenerator {

    private final AlertEngine alertEngine = new AlertEngine();
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Builds a full text summary report: overall totals, plus category-wise
     * breakdown and budget status for the given month.
     */
    public String generateSummaryReport(Account account, List<Budget> budgets, YearMonth month) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append(" PERSONAL FINANCE SUMMARY REPORT\n");
        sb.append(" Account Owner : ").append(account.getOwnerName()).append("\n");
        sb.append(" Report Month  : ").append(month).append("\n");
        sb.append(" Generated At  : ").append(java.time.LocalDateTime.now().format(TS_FMT)).append("\n");
        sb.append("=================================================================\n\n");

        double monthIncome = account.getTotalIncomeForMonth(month);
        double monthExpense = account.getTotalExpenseForMonth(month);

        sb.append("-- OVERALL (ALL-TIME) --\n");
        sb.append(String.format("Total Income   : %12.2f%n", account.getTotalIncome()));
        sb.append(String.format("Total Expense  : %12.2f%n", account.getTotalExpense()));
        sb.append(String.format("Current Balance: %12.2f%n%n", account.getBalance()));

        sb.append("-- THIS MONTH (").append(month).append(") --\n");
        sb.append(String.format("Income         : %12.2f%n", monthIncome));
        sb.append(String.format("Expense        : %12.2f%n", monthExpense));
        sb.append(String.format("Net            : %12.2f%n%n", monthIncome - monthExpense));

        sb.append("-- CATEGORY-WISE EXPENSE BREAKDOWN (").append(month).append(") --\n");
        Map<Category, Double> categoryTotals = new EnumMap<>(Category.class);
        for (Transaction t : account.getTransactionsForMonth(month)) {
            if (t.getType() == Transaction.Type.EXPENSE) {
                categoryTotals.merge(t.getCategory(), t.getAmount(), Double::sum);
            }
        }
        if (categoryTotals.isEmpty()) {
            sb.append("  (no expenses recorded this month)\n");
        } else {
            for (Map.Entry<Category, Double> entry : categoryTotals.entrySet()) {
                double pctOfTotal = monthExpense == 0 ? 0 : (entry.getValue() / monthExpense) * 100.0;
                sb.append(String.format("  %-13s : %10.2f  (%.1f%% of monthly spend)%n",
                        entry.getKey(), entry.getValue(), pctOfTotal));
            }
        }
        sb.append("\n");

        sb.append("-- BUDGET STATUS (").append(month).append(") --\n");
        List<AlertEngine.AlertResult> results = alertEngine.checkAllBudgets(account, budgets, month);
        if (results.isEmpty()) {
            sb.append("  (no budgets configured for this month)\n");
        } else {
            for (AlertEngine.AlertResult r : results) {
                sb.append("  ").append(r).append("\n");
            }
        }
        sb.append("\n=================================================================\n");
        return sb.toString();
    }

    /** Writes an arbitrary text report (e.g. from generateSummaryReport) to a plain text file. */
    public void exportTextReport(String content, String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.print(content);
        }
    }

    /**
     * Exports the raw transaction ledger to a CSV file.
     */
    public void exportTransactionsCsv(Account account, String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.println("ID,Type,Amount,Category,Date,Description");
            for (Transaction t : account.getAllTransactions()) {
                writer.printf("%d,%s,%.2f,%s,%s,%s%n",
                        t.getId(), t.getType(), t.getAmount(), t.getCategory(), t.getDate(),
                        escapeCsv(t.getDescription()));
            }
        }
    }

    /**
     * Exports a category-wise monthly summary (income/expense totals) to CSV.
     */
    public void exportCategorySummaryCsv(Account account, YearMonth month, String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Map<Category, double[]> totals = new EnumMap<>(Category.class); // [0]=income [1]=expense
        for (Transaction t : account.getTransactionsForMonth(month)) {
            totals.computeIfAbsent(t.getCategory(), c -> new double[2]);
            if (t.getType() == Transaction.Type.INCOME) {
                totals.get(t.getCategory())[0] += t.getAmount();
            } else {
                totals.get(t.getCategory())[1] += t.getAmount();
            }
        }
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            writer.println("Category,Income,Expense,Net");
            for (Map.Entry<Category, double[]> entry : totals.entrySet()) {
                double income = entry.getValue()[0];
                double expense = entry.getValue()[1];
                writer.printf("%s,%.2f,%.2f,%.2f%n", entry.getKey(), income, expense, income - expense);
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
