package pl.projekt;

import java.util.Arrays;

public class TransportProblem {
    public static final int BIG_NEGATIVE = -1_000_000;

    private final int[][] transportCosts;
    private final int[] supply;
    private final int[] demand;
    private final int[] purchasePrices;
    private final int[] sellingPrices;
    private final boolean[][] blocked;
    private final boolean[] blockedSuppliers;
    private final boolean[] fakeSuppliers;
    private final boolean[] fakeReceivers;

    public TransportProblem(int[][] transportCosts, int[] supply, int[] demand, int[] purchasePrices, int[] sellingPrices, boolean[][] blocked) {
        this(
                transportCosts,
                supply,
                demand,
                purchasePrices,
                sellingPrices,
                blocked,
                new boolean[supply.length],
                new boolean[supply.length],
                new boolean[demand.length]
        );
    }

    public TransportProblem(int[][] transportCosts, int[] supply, int[] demand, int[] purchasePrices, int[] sellingPrices, boolean[][] blocked, boolean[] blockedSuppliers) {
        this(
                transportCosts,
                supply,
                demand,
                purchasePrices,
                sellingPrices,
                blocked,
                blockedSuppliers,
                new boolean[supply.length],
                new boolean[demand.length]
        );
    }

    private TransportProblem(
            int[][] transportCosts,
            int[] supply,
            int[] demand,
            int[] purchasePrices,
            int[] sellingPrices,
            boolean[][] blocked,
            boolean[] blockedSuppliers,
            boolean[] fakeSuppliers,
            boolean[] fakeReceivers
    ) {
        this.transportCosts = copyMatrix(transportCosts);
        this.supply = Arrays.copyOf(supply, supply.length);
        this.demand = Arrays.copyOf(demand, demand.length);
        this.purchasePrices = Arrays.copyOf(purchasePrices, purchasePrices.length);
        this.sellingPrices = Arrays.copyOf(sellingPrices, sellingPrices.length);
        this.blocked = copyMatrix(blocked);
        this.blockedSuppliers = Arrays.copyOf(blockedSuppliers, blockedSuppliers.length);
        this.fakeSuppliers = Arrays.copyOf(fakeSuppliers, fakeSuppliers.length);
        this.fakeReceivers = Arrays.copyOf(fakeReceivers, fakeReceivers.length);
    }


    public int[][] getTransportCosts() { return transportCosts; }
    public int[] getSupply() { return supply; }
    public int[] getDemand() { return demand; }
    public int[] getPurchasePrices() { return purchasePrices; }
    public int[] getSellingPrices() { return sellingPrices; }
    public boolean[][] getBlocked() { return blocked; }
    public boolean[] getBlockedSuppliers() { return blockedSuppliers; }
    public boolean[] getFakeSuppliers() { return fakeSuppliers; }
    public boolean[] getFakeReceivers() { return fakeReceivers; }

    public boolean isFakeSupplier(int index) {
        return fakeSuppliers[index];
    }

    public boolean isFakeReceiver(int index) {
        return fakeReceivers[index];
    }

    public boolean isBlockedSupplier(int index) {
        return index >= 0 && index < blockedSuppliers.length && blockedSuppliers[index];
    }

    public int getSupplierCount() {
        return supply.length;
    }

    public int getReceiverCount() {
        return demand.length;
    }

    public int[][] calculateProfitMatrix() {
        int suppliers = supply.length;
        int receivers = demand.length;
        int[][] profits =  new int[suppliers][receivers];

        for (int i = 0 ; i<suppliers ; i++ ) {
            for (int j = 0 ; j<receivers ; j++ ) {
                if (fakeSuppliers[i] || fakeReceivers[j]) {
                    if (!fakeSuppliers[i] && fakeReceivers[j] && isBlockedSupplier(i)) {
                        profits[i][j] = BIG_NEGATIVE;
                    } else {
                        profits[i][j] = 0;
                    }
                } else {
                    profits[i][j] = sellingPrices[j] - purchasePrices[i] - transportCosts[i][j];
                }
            }
        }
        return profits;
    }

    public boolean supplierHasBlockedRoute(int supplier) {
        return isBlockedSupplier(supplier);
    }

    public TransportProblem balanced() {
        int totalSupply = sum(supply);
        int totalDemand = sum(demand);

        int suppliers = supply.length;
        int receivers = demand.length;

        int[][] newCosts = new int[suppliers][receivers + 1];
        boolean[][] newBlocked = new boolean[suppliers][receivers + 1];

        for (int i = 0; i < suppliers; i++) {
            System.arraycopy(transportCosts[i], 0, newCosts[i], 0, receivers);
            System.arraycopy(blocked[i], 0, newBlocked[i], 0, receivers);
        }

        int[] newDemand = Arrays.copyOf(demand, receivers + 1);
        int[] newSellingPrices = Arrays.copyOf(sellingPrices, receivers + 1);
        boolean[] newBlockedSuppliers = Arrays.copyOf(blockedSuppliers, suppliers + 1);
        boolean[] newFakeReceivers = Arrays.copyOf(fakeReceivers, receivers + 1);

        newDemand[receivers] = totalSupply;
        newSellingPrices[receivers] = 0;
        newBlockedSuppliers[suppliers] = false;
        newFakeReceivers[receivers] = true;

        int[] newSupply = Arrays.copyOf(supply, suppliers + 1);
        int[] newPurchasePrices = Arrays.copyOf(purchasePrices, suppliers + 1);
        boolean[] newFakeSuppliers = Arrays.copyOf(fakeSuppliers, suppliers + 1);

        newCosts = Arrays.copyOf(newCosts, suppliers + 1);
        newBlocked = Arrays.copyOf(newBlocked, suppliers + 1);
        newCosts[suppliers] = new int[receivers + 1];
        newBlocked[suppliers] = new boolean[receivers + 1];

        newSupply[suppliers] = totalDemand;
        newPurchasePrices[suppliers] = 0;
        newFakeSuppliers[suppliers] = true;

        return new TransportProblem(
                newCosts,
                newSupply,
                newDemand,
                newPurchasePrices,
                newSellingPrices,
                newBlocked,
                newBlockedSuppliers,
                newFakeSuppliers,
                newFakeReceivers
        );
    }

    private static int sum(int[] values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum;
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
