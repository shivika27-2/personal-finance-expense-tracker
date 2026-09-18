package com.financetracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single financial transaction (either INCOME or EXPENSE).
 */
public class Transaction {

    /** Global, monotonically increasing id generator shared by all transactions. */
    private static int idCounter = 1;

    public enum Type {
        INCOME,
        EXPENSE
    }

    private final int id;
    private final Type type;
    private final double amount;
    private final Category category;
    private final LocalDate date;
    private final String description;
    private final boolean recurringGenerated;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Creates a brand-new transaction; an id is auto-assigned.
     */
    public Transaction(Type type, double amount, Category category, LocalDate date, String description) {
        this(idCounter++, type, amount, category, date, description, false);
    }

    /**
     * Creates a transaction that was auto-generated from a recurring template.
     */
    public Transaction(Type type, double amount, Category category, LocalDate date, String description, boolean recurringGenerated) {
        this(idCounter++, type, amount, category, date, description, recurringGenerated);
    }

    /**
     * Full constructor, primarily used when reconstructing a Transaction from persisted storage
     * (an explicit id is supplied so ids remain stable across application runs).
     */
    public Transaction(int id, Type type, double amount, Category category, LocalDate date, String description, boolean recurringGenerated) {
        this.id = id;
        this.type = Objects.requireNonNull(type, "type cannot be null");
        this.amount = amount;
        this.category = Objects.requireNonNull(category, "category cannot be null");
        this.date = Objects.requireNonNull(date, "date cannot be null");
        this.description = description == null ? "" : description;
        this.recurringGenerated = recurringGenerated;
        if (id >= idCounter) {
            idCounter = id + 1;
        }
    }

    /** Ensures future auto-generated ids never collide with ids loaded from disk. */
    public static void ensureNextIdAtLeast(int minId) {
        if (minId >= idCounter) {
            idCounter = minId + 1;
        }
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRecurringGenerated() {
        return recurringGenerated;
    }

    /** Serializes this transaction into a pipe-delimited row for flat-file storage. */
    public String toStorageRow() {
        String safeDescription = description.replace("|", "/").replace("\n", " ");
        return String.join("|",
                String.valueOf(id),
                type.name(),
                String.valueOf(amount),
                category.name(),
                date.format(DATE_FMT),
                safeDescription,
                String.valueOf(recurringGenerated));
    }

    /** Rebuilds a Transaction from a row produced by {@link #toStorageRow()}. */
    public static Transaction fromStorageRow(String row) {
        String[] parts = row.split("\\|", -1);
        int id = Integer.parseInt(parts[0]);
        Type type = Type.valueOf(parts[1]);
        double amount = Double.parseDouble(parts[2]);
        Category category = Category.valueOf(parts[3]);
        LocalDate date = LocalDate.parse(parts[4], DATE_FMT);
        String description = parts[5];
        boolean recurringGenerated = parts.length > 6 && Boolean.parseBoolean(parts[6]);
        return new Transaction(id, type, amount, category, date, description, recurringGenerated);
    }

    @Override
    public String toString() {
        String sign = type == Type.INCOME ? "+" : "-";
        return String.format("#%-4d [%-4s] %-13s %s%10.2f  %-10s  %s",
                id, date.format(DATE_FMT), category, sign, amount, type, description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}