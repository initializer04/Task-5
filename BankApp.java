import java.util.*;
import java.io.*;

// Abstract Base Account Class
abstract class Account {
    protected String accountHolder;
    protected String accountNumber;
    protected double balance;
    protected List<String> transactionHistory;

    public Account(String accountHolder, String accountNumber) {
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            addTransaction("Deposited ₹" + amount);
            System.out.println("Deposited ₹" + amount);
        } else {
            System.out.println("Invalid deposit amount.");
        }
        saveToFile();
    }

    public abstract void withdraw(double amount);

    protected void addTransaction(String message) {
        String transactionId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = new Date().toString();
        transactionHistory.add("TxnID: " + transactionId + " | " + message + " | " + timestamp);
    }

    public void showTransactions() {
        System.out.println("\nTransaction History:");
        for (String t : transactionHistory) {
            System.out.println(t);
        }
    }

    public void showBalance() {
        System.out.printf("Current Balance: ₹%.2f%n", balance);
    }

    public void showAccountInfo() {
        System.out.println("\nAccount Holder: " + accountHolder);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Type: " + this.getClass().getSimpleName());
        showBalance();
    }

    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(accountNumber + ".txt"))) {
            writer.write(accountHolder + "\n");
            writer.write(accountNumber + "\n");
            writer.write(this.getClass().getSimpleName() + "\n");
            writer.write(balance + "\n");
            for (String t : transactionHistory) {
                writer.write(t + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving account: " + e.getMessage());
        }
    }

    public static Account loadFromFile(String accNumber) {
        try (BufferedReader reader = new BufferedReader(new FileReader(accNumber + ".txt"))) {
            String name = reader.readLine();
            String number = reader.readLine();
            String type = reader.readLine();
            double bal = Double.parseDouble(reader.readLine());

            Account acc;
            if ("SavingsAccount".equals(type)) {
                acc = new SavingsAccount(name, number);
            } else if ("CurrentAccount".equals(type)) {
                acc = new CurrentAccount(name, number);
            } else {
                return null;
            }

            acc.balance = bal;
            String line;
            while ((line = reader.readLine()) != null) {
                acc.transactionHistory.add(line);
            }

            return acc;
        } catch (IOException e) {
            System.out.println("Account not found or file error.");
            return null;
        }
    }
}

// Savings Account Class
class SavingsAccount extends Account {
    private final double interestRate = 0.03; // 3%

    public SavingsAccount(String accountHolder, String accountNumber) {
        super(accountHolder, accountNumber);
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= balance && amount > 0) {
            balance -= amount;
            addTransaction("Withdrew ₹" + amount);
            System.out.println("Withdrew ₹" + amount);
        } else {
            System.out.println("Insufficient balance.");
        }
        saveToFile();
    }

    public void applyInterest() {
        double interest = balance * interestRate;
        balance += interest;
        addTransaction("Interest Added: ₹" + interest);
        System.out.println("Interest of ₹" + interest + " added.");
        saveToFile();
    }
}

// Current Account Class
class CurrentAccount extends Account {
    private final double overdraftLimit = 5000.0;

    public CurrentAccount(String accountHolder, String accountNumber) {
        super(accountHolder, accountNumber);
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= balance + overdraftLimit && amount > 0) {
            balance -= amount;
            addTransaction("Withdrew ₹" + amount);
            System.out.println("Withdrew ₹" + amount);
        } else {
            System.out.println("Withdrawal exceeds overdraft limit.");
        }
        saveToFile();
    }
}

// Main Application Class
public class BankApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Account account = null;

        System.out.println("=== Welcome to Java Bank ===");
        System.out.println("1. Create New Account");
        System.out.println("2. Login to Existing Account");
        System.out.print("Enter choice: ");
        int action = sc.nextInt();
        sc.nextLine(); // Consume newline

        if (action == 1) {
            System.out.print("Enter account holder name: ");
            String name = sc.nextLine();

            System.out.println("Choose account type:\n1. Savings Account\n2. Current Account");
            int type = sc.nextInt();
            sc.nextLine(); // Consume newline
            String accNumber = "ACC" + (new Random().nextInt(90000) + 10000);

            if (type == 1) {
                account = new SavingsAccount(name, accNumber);
            } else if (type == 2) {
                account = new CurrentAccount(name, accNumber);
            } else {
                System.out.println("Invalid account type.");
                return;
            }

            account.saveToFile();
            System.out.println("Account created! Your Account Number: " + accNumber);

        } else if (action == 2) {
            System.out.print("Enter your account number: ");
            String accNumber = sc.nextLine();
            account = Account.loadFromFile(accNumber);

            if (account == null) {
                System.out.println("Login failed.");
                return;
            } else {
                System.out.println("Welcome back, " + account.accountHolder + "!");
            }

        } else {
            System.out.println("Invalid option.");
            return;
        }

        while (true) {
            System.out.println("\n==== Menu ====");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Show Balance");
            System.out.println("4. Show Transactions");
            if (account instanceof SavingsAccount) {
                System.out.println("5. Apply Interest");
            }
            System.out.println("0. Exit");

            System.out.print("Choose option: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter deposit amount: ₹");
                    account.deposit(sc.nextDouble());
                    break;
                case 2:
                    System.out.print("Enter withdraw amount: ₹");
                    account.withdraw(sc.nextDouble());
                    break;
                case 3:
                    account.showAccountInfo();
                    break;
                case 4:
                    account.showTransactions();
                    break;
                case 5:
                    if (account instanceof SavingsAccount) {
                        ((SavingsAccount) account).applyInterest();
                    } else {
                        System.out.println("Invalid option.");
                    }
                    break;
                case 0:
                    System.out.println("Thank you for banking with us!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
          }
