package pl.projekt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaxElementSolver {
    public TransportResult solve(TransportProblem problem) {
        TransportProblem balancedProblem = problem.balanced();
        int[][] profits = balancedProblem.calculateProfitMatrix();
        int[] supply = Arrays.copyOf(balancedProblem.getSupply(), balancedProblem.getSupply().length);
        int[] demand = Arrays.copyOf(balancedProblem.getDemand(), balancedProblem.getDemand().length);
        boolean[][] blocked = balancedProblem.getBlocked();

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

            allocation[bestSupplier][bestReceiver] += amount;
            supply[bestSupplier] -= amount;
            demand[bestReceiver] -= amount;

            steps.add(new IterationStep(
                    bestSupplier,
                    bestReceiver,
                    bestProfit,
                    amount,
                    allocation,
                    supply,
                    demand
            ));
        }

        int totalProfit = 0;

        for (int i = 0; i < suppliers; i++) {
            for (int j = 0; j < receivers; j++) {
                totalProfit += allocation[i][j] * profits[i][j];
            }
        }

        boolean feasible = !hasPositive(supply) && !hasPositive(demand);
        String message = feasible
                ? "Rozwiązanie znalezione."
                : "Nie udało się zrealizować całego popytu lub podaży. Sprawdź blokady tras.";

        return new TransportResult(allocation, totalProfit, steps, balancedProblem, feasible, message);
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
