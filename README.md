# 💰 Personal Finance & Expense Tracker with Budget Alerts

A command-line Java application built to help users track income and expenses, manage category-wise budgets, and get automatic alerts before they overspend — no bank linking, no ads, no bloat, just a fast menu-driven tool that remembers everything locally between runs. ⚡

## 🌟 Overview

Most people don't track where their money actually goes — they find out they've overspent only after the month is already over, when there's nothing left to fix. This project automates that: you log every income/expense as it happens, and the system continuously checks spending against your budgets and warns you the moment a category starts looking risky — not after.

Most expense-tracking apps are also heavier than they need to be for that. This one goes the other way — open it, log a transaction, check your balance, close it.

## 📌 Problem Statement

- People overspend simply because they lose track of running totals
- Manually checking "how much have I spent on Food this month?" is tedious
- Early warnings (before you're 100% over budget) let you course-correct in time
- Recurring bills (rent, subscriptions, salary) are easy to forget to log manually
- A simple, free, offline tool removes the excuse of "I don't have a budgeting app"

## 🔧 How It Works

```
User enters a transaction (Income / Expense, amount, category, date)
                ↓
  [Validation]  Account checks the transaction is valid
                (positive amount, valid category/date)
                ↓
  [Aggregation] Account tallies totals per category, per month
                ↓
  [Rule Engine] AlertEngine compares category spend vs. Budget limit
                ≥ 80%  → WARNING alert
                ≥ 100% → CRITICAL alert (BudgetExceededException)
                ↓
  [Automation]  RecurringTransaction templates auto-generate transactions
                that are due (rent, salary, subscriptions, etc.)
                ↓
  Summary Report + CSV/Text export generated on request
```

## ✨ Features

- ➕ Add / view / delete transactions, filter by category
- 📊 Set or update a monthly budget per category
- 🚦 Automatic budget alerts at 80% (⚠️ WARNING) and 100%+ (🔴 CRITICAL, throws `BudgetExceededException`)
- 🔁 Recurring transaction templates (DAILY / WEEKLY / MONTHLY / YEARLY) with a "process due transactions" routine that materializes them into real transactions
- 📈 Monthly summary report — overall totals, category breakdown, budget status
- 📤 Export to plain text report, transaction CSV, and category-summary CSV
- 💾 Local persistence via pipe-delimited flat files under `data/` — survives restarts, skips corrupted rows on load instead of crashing
- 🛡️ Custom checked exceptions (`InvalidTransactionException`, `BudgetExceededException`) instead of generic runtime exceptions

## 🏗️ Core Concepts Applied (Course Mapping)

| Course Concept | How It Is Used in This Project |
|---|---|
| Object-Oriented Design | Separate model / service / exception / util / main layers, each with a single responsibility |
| Encapsulation | `Account` hides its transaction list behind getters and controlled `addTransaction()` / `removeTransaction()` methods |
| Custom Exceptions | `InvalidTransactionException` and `BudgetExceededException` model domain-specific error cases |
| Enums | `Category`, `Transaction.Type`, and `RecurringTransaction.Frequency` model fixed sets of values type-safely |
| Rule / Threshold Engine | `AlertEngine` evaluates spend-vs-budget percentage and classifies it OK / WARNING / CRITICAL |
| File I/O & Persistence | `DataStorageUtil` reads/writes transactions, budgets, and recurring templates to local flat files |
| Streams & Aggregation | `Account` uses Java Streams (`mapToDouble`, `sum`, `filter`) to compute income/expense totals |
| Date & Time API | `java.time.LocalDate` / `YearMonth` drive monthly aggregation and recurring-schedule calculations |
| Report Generation | `ReportGenerator` builds formatted text summaries and exports CSV files |

## 🛠️ Technologies / Tools Used

- ☕ Java 17+ (tested on OpenJDK 21) — switch expressions, enum-driven modeling, modern syntax
- 📦 Java Standard Library only — `java.time` for dates, `java.nio.file` for I/O, `java.util.Scanner` for CLI input, `java.util.stream` for aggregation. No external libraries, no build tool required.
- 🔧 Git / GitHub for version control

## 🗂️ Project Structure

```
finance-tracker/
├── src/
│   └── com/financetracker/
│       ├── main/          MainApp.java              ← CLI entry point (run this)
│       ├── model/
│       │   ├── Transaction.java                     ← income/expense record
│       │   ├── Category.java                        ← enum of spending/income categories
│       │   ├── Budget.java                          ← monthly category limit
│       │   ├── Account.java                         ← balance + transaction collection
│       │   └── RecurringTransaction.java             ← recurring transaction template
│       ├── service/
│       │   ├── AlertEngine.java                     ← budget threshold rule engine
│       │   └── ReportGenerator.java                 ← text/CSV report generation
│       ├── exception/
│       │   ├── InvalidTransactionException.java
│       │   └── BudgetExceededException.java
│       └── util/
│           └── DataStorageUtil.java                 ← local flat-file persistence
├── data/                   created automatically at runtime (transactions.dat,
│                           budgets.dat, recurring.dat, exports)
├── docs/
│   ├── diagrams.md         UML / workflow diagrams (Mermaid)
│   ├── REPORT.md / .pdf    Full project report
│   └── screenshots/        Drop your terminal screenshots here
├── statement.md
└── README.md
```

## 🚀 Steps to Install & Run

**Prerequisites:** JDK 17+ installed (`java -version` to check)

### Step 1 — Clone and enter the project

```bash
git clone https://github.com/<shivika27-2>/<personal-finance-expense-tracker>.git
cd <personal-finance-expense-tracker>
```

### Step 2 — Confirm you're in the right folder

Run `pwd` (PowerShell/macOS/Linux). It should show the project root — the folder that contains **both** `src` and `bin`, e.g.:

```
C:\...\<personal-finance-expense-tracker>
```

> ⚠️ If it instead shows a path ending in `\src`, run `cd ..` first and check `pwd` again.

### Step 3 — Compile

**Windows (PowerShell):**
```powershell
javac -d bin (Get-ChildItem -Path src -Filter *.java -Recurse).FullName
```

**macOS/Linux:**
```bash
javac -d bin $(find src -name "*.java")
```

### Step 4 — Run

```powershell
java -cp bin com.financetracker.main.MainApp
```

### 🩹 Troubleshooting

| Problem | Fix |
|---|---|
| `error: no source files` | You're not in the project root — check `pwd`, `cd ..` if needed, retry Step 3 |
| `Could not find or load main class` | Same fix, or `bin` is empty/stale — delete it and recompile (`Remove-Item -Recurse -Force bin` on Windows) |
| `duplicate class` errors | A stray old `.java` file exists outside `src` — find and delete/move it |

## 💻 How to Use

Once you run the app, it will:

- Ask for your account owner name
- Show a numbered menu:
  1. Add Transaction (Income/Expense)
  2. View All Transactions
  3. View Transactions by Category
  4. Set / Update Monthly Budget
  5. View Budgets & Alerts (this month)
  6. Add Recurring Transaction
  7. View Recurring Transactions
  8. Process Due Recurring Transactions
  9. Generate Summary Report
  10. Export Summary Report to Text File
  11. Export Transactions to CSV
  12. Export Category Summary to CSV
  13. Delete a Transaction
  14. Save & Exit
- 🚨 Warn you the moment a category crosses 80% (WARNING) or 100% (CRITICAL) of its budget
- 💾 Save everything automatically to a local `data/` folder so it's there next time you run it

## 🖥️ Sample Output

```
==================================================
     PERSONAL FINANCE & EXPENSE TRACKER  -  Budget Alert Edition
==================================================
Enter account owner name: Shivika

-----------------------------------------------------------------
Account: Shivika | Balance: 0.00
-----------------------------------------------------------------
Choose an option: 1

Transaction Type: 1) INCOME  2) EXPENSE
Select type: 2
Enter amount: 450
Enter category name or number: 1 (FOOD)
Enter date (yyyy-MM-dd) or press Enter for today:
Enter description (optional): Groceries

Transaction recorded: #1 [2026-09-05] FOOD - 450.00 EXPENSE Groceries
[WARNING]  FOOD          spent     450.00 /     500.00  (90.0% of budget) [2026-09]

-----------------------------------------------------------------
Account: Shivika | Balance: 4550.00
-----------------------------------------------------------------
```

## 📊 Budget Alert Rules

| Spend vs. Budget | Result |
|---|---|
| < 80% | OK — no alert |
| ≥ 80% and < 100% | ⚠️ WARNING printed to console |
| ≥ 100% | 🚨 CRITICAL — `BudgetExceededException` raised, transaction still recorded |

The alert engine re-evaluates on every new transaction and every recurring transaction that gets processed, so alerts are always based on live, up-to-date totals — never a cached snapshot.

## 📊 Dataset / Persistence Details

| Property | Details |
|---|---|
| Storage type | Local flat files (pipe-delimited `.dat`), zero external dependencies |
| Files | `data/transactions.dat`, `data/budgets.dat`, `data/recurring.dat` |
| Categories | 15 fixed categories (Food, Transport, Housing, Utilities, Entertainment, Healthcare, Education, Shopping, Insurance, Travel, Salary, Business, Investment, Gift, Other) |
| Recurring frequencies | Daily, Weekly, Monthly, Yearly |
| Exports | Text report (`.txt`), transaction ledger (`.csv`), category summary (`.csv`) |

A custom lightweight format (instead of a database or JSON library) was used so the project compiles and runs with only the JDK — zero setup friction for anyone reviewing it.

## 📦 Dependencies

| Library | Purpose |
|---|---|
| — | None — pure Java Standard Library (`java.time`, `java.nio.file`, `java.util.stream`) |

No `pip install` / `npm install` equivalent needed — just a JDK.

## 🧪 Instructions for Testing

There's no bundled test suite (this is a menu-driven CLI, so testing is primarily manual/functional), but the validation logic is easy to exercise directly:

- 🚫 **Invalid transaction handling:** try adding a transaction with a negative or zero amount — `Account.addTransaction()` should reject it via `InvalidTransactionException` without crashing the app or corrupting the balance.
- 🚨 **Budget alerts:** set a budget (option 4), then add expenses in that category until you cross 80% and then 100% of the limit — you should see a WARNING first, then a CRITICAL alert.
- 🔁 **Recurring transactions:** add a recurring transaction (option 6) with a past start date, then run "Process Due Recurring Transactions" (option 8) — it should generate one transaction per elapsed interval and advance the next due date correctly.
- 💾 **Persistence:** add a few transactions, save & exit, then relaunch — the same balance and transaction list should reload from `data/`.
- 🩹 **Corrupted data resilience:** manually break a line in `data/transactions.dat`, relaunch, and confirm the loader skips just that line (prints a warning) instead of failing to start.

## 🚧 Challenges Faced

- Deciding whether a budget breach should block a transaction or just warn — chose to still record the transaction and raise the alert, since blocking money you've already spent isn't realistic
- Keeping recurring transactions accurate when the app hasn't been opened in a while — solved by looping and "catching up" one cycle at a time until the template is no longer overdue
- Avoiding external dependencies while still supporting persistence — solved with a simple custom flat-file format instead of JSON/a database

## 🔮 Future Improvements

- Multi-currency and multi-account support
- Configurable alert thresholds instead of the fixed 80% / 100% rule
- Switch storage to JSON or an embedded database (e.g. SQLite)
- Add a simple GUI (JavaFX) or web frontend on top of the same service layer
- Email/SMS notifications when a CRITICAL alert is triggered
- Charts for spending trends over multiple months

## 👤 Author

**Name:** Shivika Patidar
git hub repo  personal-finance-expense-tracker
