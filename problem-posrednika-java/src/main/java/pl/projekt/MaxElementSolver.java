package pl.projekt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MaxElementSolver {
    private static final int MAX_OPTIMIZATION_ITERATIONS = 100;

    public TransportResult solve(TransportProblem problem) {
        TransportProblem balancedProblem = problem.balanced();
        int[][] profits = balancedProblem.calculateProfitMatrix();
        int[] supply = Arrays.copyOf(balancedProblem.getSupply(), balancedProblem.getSupply().length);
        int[] demand = Arrays.copyOf(balancedProblem.getDemand(), balancedProblem.getDemand().length);
        boolean[][] blocked = balancedProblem.getBlocked();

        int suppliers = supply.length;
        int receivers = demand.length;

        int[][] allocation = new int[suppliers][receivers];
        boolean[][] basis = new boolean[suppliers][receivers];
        List<IterationStep> steps = new ArrayList<>();
        List<OptimizationStep> optimizationSteps = new ArrayList<>();

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
            basis[bestSupplier][bestReceiver] = true;
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

        boolean feasible = !hasPositive(supply) && !hasPositive(demand);

        if (feasible) {
            completeDegenerateBasis(basis, suppliers, receivers);
            optimize(profits, allocation, basis, optimizationSteps);
        }

        int totalProfit = calculateTotalProfit(allocation, profits);

        String message = feasible
                ? "Rozwiązanie znalezione."
                : "Nie udało się zrealizować całego popytu lub podaży. Sprawdź blokady tras.";

        return new TransportResult(allocation, totalProfit, steps, optimizationSteps, balancedProblem, feasible, message);
    }

    private void optimize(int[][] profits, int[][] allocation, boolean[][] basis, List<OptimizationStep> optimizationSteps) {
        int suppliers = allocation.length;
        int receivers = allocation[0].length;

        for (int iteration = 0; iteration < MAX_OPTIMIZATION_ITERATIONS; iteration++) {
            Integer[] alpha = new Integer[suppliers];
            Integer[] beta = new Integer[receivers];
            calculatePotentials(profits, basis, alpha, beta);

            int[][] deltas = calculateDeltas(profits, basis, alpha, beta);
            int enteringSupplier = -1;
            int enteringReceiver = -1;
            int bestDelta = 0;

            for (int i = 0; i < suppliers; i++) {
                for (int j = 0; j < receivers; j++) {
                    if (!basis[i][j] && deltas[i][j] > bestDelta) {
                        bestDelta = deltas[i][j];
                        enteringSupplier = i;
                        enteringReceiver = j;
                    }
                }
            }

            int[][] allocationBefore = copyMatrix(allocation);
            boolean[][] basisBefore = copyMatrix(basis);

            if (enteringSupplier == -1) {
                optimizationSteps.add(new OptimizationStep(
                        allocationBefore,
                        allocationBefore,
                        basisBefore,
                        alpha,
                        beta,
                        deltas,
                        -1,
                        -1,
                        0,
                        new int[suppliers][receivers],
                        true
                ));
                return;
            }

            List<Cell> path = findBasisPath(basis, enteringSupplier, enteringReceiver);
            if (path.isEmpty()) {
                optimizationSteps.add(new OptimizationStep(
                        allocationBefore,
                        allocationBefore,
                        basisBefore,
                        alpha,
                        beta,
                        deltas,
                        enteringSupplier,
                        enteringReceiver,
                        0,
                        new int[suppliers][receivers],
                        true
                ));
                return;
            }

            int[][] cycleSigns = new int[suppliers][receivers];
            cycleSigns[enteringSupplier][enteringReceiver] = 1;
            for (int index = 0; index < path.size(); index++) {
                Cell cell = path.get(index);
                cycleSigns[cell.row][cell.column] = index % 2 == 0 ? -1 : 1;
            }

            int theta = Integer.MAX_VALUE;
            Cell leavingCell = null;
            for (Cell cell : path) {
                if (cycleSigns[cell.row][cell.column] == -1 && allocation[cell.row][cell.column] < theta) {
                    theta = allocation[cell.row][cell.column];
                    leavingCell = cell;
                }
            }

            if (leavingCell == null || theta == Integer.MAX_VALUE) {
                return;
            }

            for (int i = 0; i < suppliers; i++) {
                for (int j = 0; j < receivers; j++) {
                    if (cycleSigns[i][j] == 1) {
                        allocation[i][j] += theta;
                    } else if (cycleSigns[i][j] == -1) {
                        allocation[i][j] -= theta;
                    }
                }
            }

            basis[enteringSupplier][enteringReceiver] = true;
            basis[leavingCell.row][leavingCell.column] = false;

            optimizationSteps.add(new OptimizationStep(
                    allocationBefore,
                    allocation,
                    basisBefore,
                    alpha,
                    beta,
                    deltas,
                    enteringSupplier,
                    enteringReceiver,
                    theta,
                    cycleSigns,
                    false
            ));
        }
    }

    private void calculatePotentials(int[][] profits, boolean[][] basis, Integer[] alpha, Integer[] beta) {
        int suppliers = basis.length;
        int receivers = basis[0].length;
        alpha[0] = 0;

        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < suppliers; i++) {
                for (int j = 0; j < receivers; j++) {
                    if (!basis[i][j]) {
                        continue;
                    }
                    if (alpha[i] != null && beta[j] == null) {
                        beta[j] = profits[i][j] - alpha[i];
                        changed = true;
                    } else if (alpha[i] == null && beta[j] != null) {
                        alpha[i] = profits[i][j] - beta[j];
                        changed = true;
                    }
                }
            }
        } while (changed);

        for (int i = 0; i < suppliers; i++) {
            if (alpha[i] == null) {
                alpha[i] = 0;
                calculatePotentials(profits, basis, alpha, beta);
                return;
            }
        }

        for (int j = 0; j < receivers; j++) {
            if (beta[j] == null) {
                beta[j] = 0;
            }
        }
    }

    private int[][] calculateDeltas(int[][] profits, boolean[][] basis, Integer[] alpha, Integer[] beta) {
        int[][] deltas = new int[profits.length][profits[0].length];

        for (int i = 0; i < profits.length; i++) {
            for (int j = 0; j < profits[i].length; j++) {
                if (!basis[i][j]) {
                    deltas[i][j] = profits[i][j] - alpha[i] - beta[j];
                }
            }
        }

        return deltas;
    }

    private List<Cell> findBasisPath(boolean[][] basis, int enteringSupplier, int enteringReceiver) {
        List<Cell> path = new ArrayList<>();
        boolean[] visitedRows = new boolean[basis.length];
        boolean[] visitedColumns = new boolean[basis[0].length];

        if (findPathFromRow(basis, enteringSupplier, enteringReceiver, visitedRows, visitedColumns, path)) {
            return path;
        }

        return List.of();
    }

    private boolean findPathFromRow(
            boolean[][] basis,
            int row,
            int targetColumn,
            boolean[] visitedRows,
            boolean[] visitedColumns,
            List<Cell> path
    ) {
        visitedRows[row] = true;

        for (int column = 0; column < basis[row].length; column++) {
            if (!basis[row][column] || visitedColumns[column]) {
                continue;
            }

            Cell cell = new Cell(row, column);
            path.add(cell);
            if (column == targetColumn) {
                return true;
            }

            visitedColumns[column] = true;
            if (findPathFromColumn(basis, column, targetColumn, visitedRows, visitedColumns, path)) {
                return true;
            }
            path.remove(path.size() - 1);
        }

        return false;
    }

    private boolean findPathFromColumn(
            boolean[][] basis,
            int column,
            int targetColumn,
            boolean[] visitedRows,
            boolean[] visitedColumns,
            List<Cell> path
    ) {
        for (int row = 0; row < basis.length; row++) {
            if (!basis[row][column] || visitedRows[row]) {
                continue;
            }

            Cell cell = new Cell(row, column);
            path.add(cell);
            visitedRows[row] = true;
            if (findPathFromRow(basis, row, targetColumn, visitedRows, visitedColumns, path)) {
                return true;
            }
            path.remove(path.size() - 1);
        }

        return false;
    }

    private void completeDegenerateBasis(boolean[][] basis, int suppliers, int receivers) {
        int required = suppliers + receivers - 1;

        while (countBasisCells(basis) < required) {
            boolean added = false;
            for (int i = 0; i < suppliers && !added; i++) {
                for (int j = 0; j < receivers && !added; j++) {
                    if (!basis[i][j]) {
                        basis[i][j] = true;
                        if (findBasisPathWithoutCell(basis, i, j)) {
                            basis[i][j] = false;
                        } else {
                            added = true;
                        }
                    }
                }
            }

            if (!added) {
                return;
            }
        }
    }

    private boolean findBasisPathWithoutCell(boolean[][] basis, int supplier, int receiver) {
        basis[supplier][receiver] = false;
        boolean hasPath = !findBasisPath(basis, supplier, receiver).isEmpty();
        basis[supplier][receiver] = true;
        return hasPath;
    }

    private int countBasisCells(boolean[][] basis) {
        int count = 0;
        for (boolean[] row : basis) {
            for (boolean value : row) {
                if (value) {
                    count++;
                }
            }
        }
        return count;
    }

    private int calculateTotalProfit(int[][] allocation, int[][] profits) {
        int totalProfit = 0;

        for (int i = 0; i < allocation.length; i++) {
            for (int j = 0; j < allocation[i].length; j++) {
                totalProfit += allocation[i][j] * profits[i][j];
            }
        }

        return totalProfit;
    }

    private boolean hasPositive(int[] array) {
        for (int value : array) {
            if (value > 0) {
                return true;
            }
        }
        return false;
    }

    private int[][] copyMatrix(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }

    private boolean[][] copyMatrix(boolean[][] matrix) {
        boolean[][] copy = new boolean[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }

    private static class Cell {
        private final int row;
        private final int column;

        private Cell(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }
}
