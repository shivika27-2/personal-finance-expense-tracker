package com.financetracker.model;

/**
 * Represents the category a transaction belongs to.
 * Categories are shared between INCOME and EXPENSE transactions;
 * the {@link Transaction.Type} field on a Transaction determines
 * how a category is actually being used.
 */
public enum Category {
    FOOD,
    TRANSPORT,
    HOUSING,
    UTILITIES,
    ENTERTAINMENT,
    HEALTHCARE,
    EDUCATION,
    SHOPPING,
    INSURANCE,
    TRAVEL,
    SALARY,
    BUSINESS,
    INVESTMENT,
    GIFT,
    OTHER;

    /**
     * Parses a category from user input in a case-insensitive, forgiving way.
     *
     * @param input raw string typed by the user
     * @return the matching Category
     * @throws IllegalArgumentException if no category matches
     */
    public static Category fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return Category.valueOf(input.trim().toUpperCase());
    }

    public static void printAll() {
        Category[] values = Category.values();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(String.format("%2d. %-13s", i + 1, values[i]));
            if ((i + 1) % 3 == 0) {
                sb.append("\n");
            }
        }
        System.out.println(sb.toString().stripTrailing());
    }
}