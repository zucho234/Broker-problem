package pl.projekt;

import java.util.List;

public class TransportResult {
    private int[][] allocation;
    private int totalProfit;
    private List<IterationStep> steps;

    public TransportResult(int[][] allocation, int totalProfit, List<IterationStep> steps) {
        this.allocation = allocation;
        this.totalProfit = totalProfit;
        this.steps = steps;
    }

    public int[][] getAllocation() {
        return allocation;
    }

    public int getTotalProfit() {
        return totalProfit;
    }

    public List<IterationStep> getSteps() {
        return steps;
    }
}
