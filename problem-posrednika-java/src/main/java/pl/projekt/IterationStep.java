package pl.projekt;

import java.util.Arrays;

public class IterationStep {
    private final int supplier;
    private final int receiver;
    private final int profit;
    private final int amount;
    private final int[][] allocationSnapshot;
    private final int[] remainingSupply;
    private final int[] remainingDemand;

    public IterationStep(int supplier, int receiver, int profit, int amount) {
        this(supplier, receiver, profit, amount, new int[0][0], new int[0], new int[0]);
    }

    public IterationStep(
            int supplier,
            int receiver,
            int profit,
            int amount,
            int[][] allocationSnapshot,
            int[] remainingSupply,
            int[] remainingDemand
    ) {
        this.supplier = supplier;
        this.receiver = receiver;
        this.profit = profit;
        this.amount = amount;
        this.allocationSnapshot = copyMatrix(allocationSnapshot);
        this.remainingSupply = Arrays.copyOf(remainingSupply, remainingSupply.length);
        this.remainingDemand = Arrays.copyOf(remainingDemand, remainingDemand.length);
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

    public int[][] getAllocationSnapshot() {
        return copyMatrix(allocationSnapshot);
    }

    public int[] getRemainingSupply() {
        return Arrays.copyOf(remainingSupply, remainingSupply.length);
    }

    public int[] getRemainingDemand() {
        return Arrays.copyOf(remainingDemand, remainingDemand.length);
    }

    private static int[][] copyMatrix(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }
}
