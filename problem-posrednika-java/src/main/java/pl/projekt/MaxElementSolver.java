package pl.projekt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaxElementSolver {
    public TransportResult solve(TransportProblem problem) {
        int[][] profits = problem.calculateProfitMatrix();
        int[] supply = Arrays.copyOf(problem.getSupply(), problem.getSupply().length);
        int[] demand = Arrays.copyOf(problem.getDemand(), problem.getDemand().length);
        boolean[][] blocked = problem.getBlocked();

        int suppliers = supply.length;
        int receivers = demand.length;

        int[][] allocation = new int[suppliers][receivers];
        List<IterationStep> steps = new ArrayList<>();

        while (hasPositive(supply) && hasPositive(demand)) {
            int bestSupplier = -1;
            int bestReceiver = -1;
            int bestProfit = Integer.MIN_VALUE;

            for (int i = 0; i < suppliers; i++) {
                for (int j = 0; j < receivers; j++) {
                    if (supply[i] > 0 && demand[j] > 0 && !blocked[i][j]) {
                        if (profits[i][j] > bestProfit) {
                            bestProfit = profits[i][j];
                            bestSupplier = i;
                            bestReceiver = j;
                        }
                    }
                }
            }

            if (bestSupplier == -1) {
                break;
            }

            int amount = Math.min(supply[bestSupplier], demand[bestReceiver]);

            allocation[bestSupplier][bestReceiver] = amount;
            supply[bestSupplier] -= amount;
            demand[bestReceiver] -= amount;

            steps.add(new IterationStep(bestSupplier, bestReceiver, bestProfit, amount));
        }

        int totalProfit = 0;

        for (int i = 0; i < suppliers; i++) {
            for (int j = 0; j < receivers; j++) {
                totalProfit += allocation[i][j] * profits[i][j];
            }
        }

        return new TransportResult(allocation, totalProfit, steps);
    }

    private boolean hasPositive(int[] array) {
        for (int value : array) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }
}
