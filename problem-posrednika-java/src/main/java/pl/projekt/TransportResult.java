package pl.projekt;

import java.util.List;

public class TransportResult {
    private final int[][] allocation;
    private final int totalProfit;
    private final List<IterationStep> steps;
    private final TransportProblem problem;
    private final boolean feasible;
    private final String message;

    public TransportResult(int[][] allocation, int totalProfit, List<IterationStep> steps) {
        this(allocation, totalProfit, steps, null, true, "");
    }

    public TransportResult(
            int[][] allocation,
            int totalProfit,
            List<IterationStep> steps,
            TransportProblem problem,
            boolean feasible,
            String message
    ) {
        this.allocation = allocation;
        this.totalProfit = totalProfit;
        this.steps = steps;
        this.problem = problem;
        this.feasible = feasible;
        this.message = message;
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

    public TransportProblem getProblem() {
        return problem;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public String getMessage() {
        return message;
    }
}
