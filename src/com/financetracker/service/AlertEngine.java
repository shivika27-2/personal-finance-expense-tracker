package com.financetracker.service;

import com.financetracker.exception.BudgetExceededException;
import com.financetracker.model.Account;
import com.financetracker.model.Budget;
import com.financetracker.model.Category;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule engine that compares actual category spending against configured
 * budgets and raises alerts at configurable warning thresholds.
 */
public class AlertEngine {

    /** Percentage of budget at which a WARNING alert is issued. */
    public static final double WARNING_THRESHOLD_PERCENT = 80.0;

    /** Percentage of budget at which spending is considered fully exceeded. */
    public static final double CRITICAL_THRESHOLD_PERCENT = 100.0;

    /**
     * Represents the outcome of evaluating a single budget.
     */
    public static class AlertResult {
        public final Budget budget;
        public final double spent;
        public final double percentUsed;
        public final Level level;

        public enum Level { OK, WARNING, CRITICAL }

        public AlertResult(Budget budget, double spent, double percentUsed, Level level) {
            this.budget = budget;
            this.spent = spent;
            this.percentUsed = percentUsed;
            this.level = level;
        }

        @Override
        public String toString() {
            String icon = switch (level) {
                case OK -> "[OK]      ";
                case WARNING -> "[WARNING] ";
                case CRITICAL -> "[CRITICAL]";
            };
            return String.format("%s %-13s spent %10.2f / %10.2f  (%.1f%% of budget) [%s]",
                    icon, budget.getCategory(), spent, budget.getLimitAmount(), percentUsed, budget.getYearMonth());
        }
    }

    /**
     * Evaluates a single budget against the account's actual spending and
     * throws a BudgetExceededException if spending is at or above 100%.
     * Prints a WARNING to stdout if spending is between the warning threshold
     * and 100%, but does not throw in that case.
     *
     * @return the AlertResult describing the evaluation, if no exception is thrown
     * @throws BudgetExceededException if spending has reached/exceeded the budget limit
     */
    public AlertResult evaluate(Account account, Budget budget) throws BudgetExceededException {
        double spent = account.getTotalSpentForCategory(budget.getCategory(), budget.getYearMonth());
        double limit = budget.getLimitAmount();
        double percent = limit <= 0 ? 0.0 : (spent / limit) * 100.0;

        if (percent >= CRITICAL_THRESHOLD_PERCENT) {
            String msg = String.format(
                    "Budget exceeded for %s: spent %.2f of %.2f limit (%.1f%%) in %s",
                    budget.getCategory(), spent, limit, percent, budget.getYearMonth());
            System.out.println("!! CRITICAL ALERT !! " + msg);
            throw new BudgetExceededException(msg, percent, spent - limit);
        } else if (percent >= WARNING_THRESHOLD_PERCENT) {
            AlertResult result = new AlertResult(budget, spent, percent, AlertResult.Level.WARNING);
            System.out.println(result);
            return result;
        } else {
            return new AlertResult(budget, spent, percent, AlertResult.Level.OK);
        }
    }

    /**
     * Evaluates every budget for the given month without throwing;
     * any CRITICAL results are caught internally and converted into AlertResults
     * so callers can render a full dashboard in one pass.
     */
    public List<AlertResult> checkAllBudgets(Account account, List<Budget> budgets, YearMonth month) {
        List<AlertResult> results = new ArrayList<>();
        for (Budget budget : budgets) {
            if (!budget.getYearMonth().equals(month)) {
                continue;
            }
            double spent = account.getTotalSpentForCategory(budget.getCategory(), budget.getYearMonth());
            double limit = budget.getLimitAmount();
            double percent = limit <= 0 ? 0.0 : (spent / limit) * 100.0;
            AlertResult.Level level;
            if (percent >= CRITICAL_THRESHOLD_PERCENT) {
                level = AlertResult.Level.CRITICAL;
            } else if (percent >= WARNING_THRESHOLD_PERCENT) {
                level = AlertResult.Level.WARNING;
            } else {
                level = AlertResult.Level.OK;
            }
            results.add(new AlertResult(budget, spent, percent, level));
        }
        return results;
    }

    /** Convenience: finds the budget matching a category+month, or null if none configured. */
    public Budget findBudget(List<Budget> budgets, Category category, YearMonth month) {
        for (Budget b : budgets) {
            if (b.getCategory() == category && b.getYearMonth().equals(month)) {
                return b;
            }
        }
        return null;
    }
}
