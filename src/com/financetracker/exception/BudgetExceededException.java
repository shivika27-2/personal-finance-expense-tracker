package com.financetracker.exception;

/**
 * Thrown by the alert engine when spending in a category has
 * reached or exceeded 100% of its configured monthly budget limit.
 */
public class BudgetExceededException extends Exception {

    private final double percentageUsed;
    private final double amountOver;

    public BudgetExceededException(String message, double percentageUsed, double amountOver) {
        super(message);
        this.percentageUsed = percentageUsed;
        this.amountOver = amountOver;
    }

    public double getPercentageUsed() {
        return percentageUsed;
    }

    public double getAmountOver() {
        return amountOver;
    }
}