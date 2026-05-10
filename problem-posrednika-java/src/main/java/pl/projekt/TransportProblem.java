package pl.projekt;

public class TransportProblem {
    private int[][] transportCosts;
    private int[] supply;
    private int[] demand;
    private int[] purchasePrices;
    private int[] sellingPrices;
    private boolean[][] blocked;

    public TransportProblem(int[][] transportCosts, int[] supply, int[] demand, int[] purchasePrices, int[] sellingPrices, boolean[][] blocked) {
        this.transportCosts = transportCosts;
        this.supply = supply;
        this.demand = demand;
        this.purchasePrices = purchasePrices;
        this.sellingPrices = sellingPrices;
        this.blocked = blocked;
    }

    public int[][] getTransportCosts() { return transportCosts; }
    public int[] getSupply() { return supply; }
    public int[] getDemand() { return demand; }
    public int[] getPurchasePrices() { return purchasePrices; }
    public int[] getSellingPrices() { return sellingPrices; }
    public boolean[][] getBlocked() { return blocked; }

    public int[][] calculateProfitMatrix() {
        int suppliers = supply.length;
        int receivers = demand.length;
        int[][] profits =  new int[suppliers][receivers];

        for (int i = 0 ; i<suppliers ; i++ ) {
            for (int j = 0 ; j<receivers ; j++ ) {
                profits[i][j] = sellingPrices[j] - purchasePrices[i] - transportCosts[i][j];
            }
        }
        return profits;
    }
}
