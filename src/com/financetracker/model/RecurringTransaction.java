package com.financetracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a template for a transaction that repeats on a fixed schedule
 * (e.g. monthly rent, weekly allowance, yearly insurance premium).
 * The RecurringTransaction itself never touches an Account directly;
 * instead, when due, it is "materialized" into a real {@link Transaction}
 * by the code driving it (see MainApp's recurring-processing routine).
 */
public class RecurringTransaction {

    private static int idCounter = 1;

    public enum Frequency {
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final int id;
    private final Transaction.Type type;
    private final double amount;
    private final Category category;
    private final String description;
    private final Frequency frequency;
    private LocalDate nextDueDate;
    private boolean active;

    public RecurringTransaction(Transaction.Type type, double amount, Category category,
                                 String description, Frequency frequency, LocalDate startDate) {
        this(idCounter++, type, amount, category, description, frequency, startDate, true);
    }

    public RecurringTransaction(int id, Transaction.Type type, double amount, Category category,
                                 String description, Frequency frequency, LocalDate nextDueDate, boolean active) {
        this.id = id;
        this.type = Objects.requireNonNull(type);
        this.amount = amount;
        this.category = Objects.requireNonNull(category);
        this.description = description == null ? "" : description;
        this.frequency = Objects.requireNonNull(frequency);
        this.nextDueDate = Objects.requireNonNull(nextDueDate);
        this.active = active;
        if (id >= idCounter) {
            idCounter = id + 1;
        }
    }

    public int getId() {
        return id;
    }

    public Transaction.Type getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /** True if this recurring item is active and its next due date has arrived (or passed). */
    public boolean isDue(LocalDate asOf) {
        return active && !nextDueDate.isAfter(asOf);
    }

    /**
     * Builds the concrete Transaction for the current due date, then advances
     * nextDueDate forward according to this template's frequency.
     */
    public Transaction materialize() {
        Transaction t = new Transaction(type, amount, category, nextDueDate,
                description.isBlank() ? "(recurring)" : description + " (recurring)", true);
        advanceNextDueDate();
        return t;
    }

    private void advanceNextDueDate() {
        switch (frequency) {
            case DAILY -> nextDueDate = nextDueDate.plusDays(1);
            case WEEKLY -> nextDueDate = nextDueDate.plusWeeks(1);
            case MONTHLY -> nextDueDate = nextDueDate.plusMonths(1);
            case YEARLY -> nextDueDate = nextDueDate.plusYears(1);
        }
    }

    public String toStorageRow() {
        String safeDescription = description.replace("|", "/").replace("\n", " ");
        return String.join("|",
                String.valueOf(id), type.name(), String.valueOf(amount), category.name(),
                safeDescription, frequency.name(), nextDueDate.format(DATE_FMT), String.valueOf(active));
    }

    public static RecurringTransaction fromStorageRow(String row) {
        String[] p = row.split("\\|", -1);
        int id = Integer.parseInt(p[0]);
        Transaction.Type type = Transaction.Type.valueOf(p[1]);
        double amount = Double.parseDouble(p[2]);
        Category category = Category.valueOf(p[3]);
        String description = p[4];
        Frequency frequency = Frequency.valueOf(p[5]);
        LocalDate nextDueDate = LocalDate.parse(p[6], DATE_FMT);
        boolean active = Boolean.parseBoolean(p[7]);
        return new RecurringTransaction(id, type, amount, category, description, frequency, nextDueDate, active);
    }

    @Override
    public String toString() {
        return String.format("#%-3d [%s] %-13s %-8s %10.2f  next due: %s  %s  %s",
                id, type, category, frequency, amount, nextDueDate.format(DATE_FMT),
                description, active ? "ACTIVE" : "PAUSED");
    }
}