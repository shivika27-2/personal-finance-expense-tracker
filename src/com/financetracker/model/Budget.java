package com.financetracker.model;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a monthly spending limit for a specific category.
 * Exactly one Budget should exist per (category, yearMonth) pair.
 */
public class Budget {

    private static final DateTimeFormatter YM_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final Category category;
    private double limitAmount;
    private final YearMonth yearMonth;

    public Budget(Category category, double limitAmount, YearMonth yearMonth) {
        this.category = Objects.requireNonNull(category);
        this.limitAmount = limitAmount;
        this.yearMonth = Objects.requireNonNull(yearMonth);
    }

    public Category getCategory() {
        return category;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public String toStorageRow() {
        return String.join("|", category.name(), String.valueOf(limitAmount), yearMonth.format(YM_FMT));
    }

    public static Budget fromStorageRow(String row) {
        String[] parts = row.split("\\|", -1);
        Category category = Category.valueOf(parts[0]);
        double limit = Double.parseDouble(parts[1]);
        YearMonth ym = YearMonth.parse(parts[2], YM_FMT);
        return new Budget(category, limit, ym);
    }

    @Override
    public String toString() {
        return String.format("%-13s | Limit: %10.2f | Month: %s", category, limitAmount, yearMonth.format(YM_FMT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Budget)) return false;
        Budget budget = (Budget) o;
        return category == budget.category && yearMonth.equals(budget.yearMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, yearMonth);
    }
}