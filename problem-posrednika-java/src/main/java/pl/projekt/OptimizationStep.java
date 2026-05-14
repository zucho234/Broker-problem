package pl.projekt;

import java.util.Arrays;

public class OptimizationStep {
    private final int[][] allocationBefore;
    private final int[][] allocationAfter;
    private final boolean[][] basisBefore;
    private final Integer[] alpha;
    private final Integer[] beta;
    private final int[][] deltas;
    private final int enteringSupplier;
    private final int enteringReceiver;
    private final int theta;
    private final int[][] cycleSigns;
    private final boolean optimal;

    public OptimizationStep(
            int[][] allocationBefore,
            int[][] allocationAfter,
            boolean[][] basisBefore,
            Integer[] alpha,
            Integer[] beta,
            int[][] deltas,
            int enteringSupplier,
            int enteringReceiver,
            int theta,
            int[][] cycleSigns,
            boolean optimal
    ) {
        this.allocationBefore = copyMatrix(allocationBefore);
        this.allocationAfter = copyMatrix(allocationAfter);
        this.basisBefore = copyMatrix(basisBefore);
        this.alpha = Arrays.copyOf(alpha, alpha.length);
        this.beta = Arrays.copyOf(beta, beta.length);
        this.deltas = copyMatrix(deltas);
        this.enteringSupplier = enteringSupplier;
        this.enteringReceiver = enteringReceiver;
        this.theta = theta;
        this.cycleSigns = copyMatrix(cycleSigns);
        this.optimal = optimal;
    }

    public int[][] getAllocationBefore() {
        return copyMatrix(allocationBefore);
    }

    public int[][] getAllocationAfter() {
        return copyMatrix(allocationAfter);
    }

    public boolean[][] getBasisBefore() {
        return copyMatrix(basisBefore);
    }

    public Integer[] getAlpha() {
        return Arrays.copyOf(alpha, alpha.length);
    }

    public Integer[] getBeta() {
        return Arrays.copyOf(beta, beta.length);
    }

    public int[][] getDeltas() {
        return copyMatrix(deltas);
    }

    public int getEnteringSupplier() {
        return enteringSupplier;
    }

    public int getEnteringReceiver() {
        return enteringReceiver;
    }

    public int getTheta() {
        return theta;
    }

    public int[][] getCycleSigns() {
        return copyMatrix(cycleSigns);
    }

    public boolean isOptimal() {
        return optimal;
    }

    private static int[][] copyMatrix(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }

    private static boolean[][] copyMatrix(boolean[][] matrix) {
        boolean[][] copy = new boolean[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }
}
