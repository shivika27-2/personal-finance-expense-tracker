## 📝 Problem Statement

Let's be honest — tracking your own spending by hand is *tedious*. 😩 You tell yourself you'll remember that chai run, that late-night food delivery order, that "just this once" impulse buy… and by the time the month wraps up, your bank balance looks nothing like what you expected. Bank statements don't help much either — a wall of cryptic merchant codes with zero context, zero running totals by category, and zero warning before you blow past what you meant to spend. 📉
There's no shortage of finance apps out there, but a lot of them are overkill for what you actually need: 🏦 bank-account linking, 📢 ad notifications, premium-feature nags, and a UI you have to wait for just to log ₹150 on coffee. Sometimes you just want to open something, type a number, and get back to your day.

So — **enter the CLI.** ⚡ No loading spinners, no bloat, no internet required. This project is a lightweight, fully offline command-line personal finance tracker built in Java that:

- 💰 Records income and expense transactions with real validation
- 🔁 Handles recurring transactions (rent, subscriptions, salary) so you're not re-typing the same thing every month
- 🚨 Warns you **before** a budget is fully blown, not after the damage is done
- 💾 Saves everything locally — close the terminal, come back tomorrow, your data is still there

## 🎯 Scope of the Project

**✅ In scope:**
- Single-user, single-account CLI application
- Income/expense logging, category-based budgeting, recurring transactions
- Local flat-file persistence (no external database)
- Text/CSV report generation and export

**🚧 Out of scope** *(see Future Enhancements in the project report)*:
- Multi-user support / authentication
- Bank account integration or live transaction import
- A graphical user interface
- Cloud sync or database-backed storage (SQLite, etc.)

## 👥 Target Users

- 🎓 Students and early-career folks managing a personal budget on one device, who want something fast and clutter-free instead of a full mobile app
- ⌨️ Anyone who's more comfortable typing a command than tapping through five screens
- 📚 Used as a course project to demonstrate solid object-oriented design, custom exception handling, and file-based persistence in core Java

## ✨ High-Level Features

1. 🧾 **Transaction management** — add, view (all / by category), and delete income or expense transactions, each validated before it's recorded
2. 📊 **Budgeting & alerts** — set a monthly limit per category; the `AlertEngine` fires a WARNING (≥80%) or CRITICAL (≥100%, via `BudgetExceededException`) alert automatically
3. 🔁 **Recurring transactions** — define DAILY / WEEKLY / MONTHLY / YEARLY templates that get "processed" into real transactions when due
4. 📈 **Reporting** — an on-screen monthly summary (totals, category breakdown, budget status) plus export to plain text and CSV
5. 💾 **Local persistence** — everything saved to flat files under `data/` and reloaded automatically next time, with corrupted-line tolerance built in
