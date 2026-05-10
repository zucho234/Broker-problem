package pl.projekt;

public class IterationStep {
    private int supplier;
    private int receiver;
    private int profit;
    private int amount;

    public IterationStep(int supplier, int receiver, int profit, int amount) {
        this.supplier = supplier;
        this.receiver = receiver;
        this.profit = profit;
        this.amount = amount;
    }

    public int getSupplier() {
        return supplier;
    }

    public int getReceiver() {
        return receiver;
    }

    public int getProfit() {
        return profit;
    }

    public int getAmount() {
        return amount;
    }
}