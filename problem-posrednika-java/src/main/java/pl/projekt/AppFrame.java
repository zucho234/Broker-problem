package pl.projekt;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class AppFrame extends JFrame {
    private JTextField suppliersField;
    private JTextField receiversField;
    private JTable dataTable;
    private JTextArea resultArea;



    public AppFrame() {
        setTitle("Problem pośrednika");
        setSize(1000,800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        suppliersField = new JTextField("3");
        receiversField = new JTextField("3");

        JButton createTableButton = new JButton("Utwórz tabelę");
        createTableButton.addActionListener(e -> createTable());

        JButton calculateButton = new JButton("Oblicz");
        calculateButton.addActionListener(e -> calculate());

        JPanel topPanel = new JPanel(new GridLayout(2,2,10,10));
        topPanel.add(new JLabel("Liczba dostawców:"));
        topPanel.add(suppliersField);
        topPanel.add(new JLabel("Liczba odbiorców:"));
        topPanel.add(receiversField);

        JPanel buttonsPanel = new JPanel(new GridLayout(2,1,5,5));
        buttonsPanel.add(createTableButton);
        buttonsPanel.add(calculateButton);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.CENTER);
        northPanel.add(buttonsPanel, BorderLayout.EAST);

        dataTable = new JTable();

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setText("Tutaj pojawi się wynik działania programu.");

        //add(northPanel, BorderLayout.NORTH);
        //add(new JScrollPane(dataTable), BorderLayout.CENTER);
        //add(new JScrollPane(resultArea), BorderLayout.SOUTH);
        javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(dataTable),
                new JScrollPane(resultArea)
        );

        splitPane.setResizeWeight(0.65);
        splitPane.setDividerLocation(350);

        add(northPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        createTable();
    }

    private void createTable() {
        int suppliers = Integer.parseInt(suppliersField.getText());
        int receivers = Integer.parseInt(receiversField.getText());

        String[] columns = new String[receivers+3];
        columns[0] = "";
        for (int i = 1; i <= receivers; i++) {
            columns[i] = "0" + i;
        }
        columns[receivers + 1] = "Podaż";
        columns[receivers + 2] = "Cena zakupu";

        String[][] data = new String[suppliers + 2][receivers + 3];

        for (int i = 0; i < suppliers; i++) {
            data[i][0] = "D" + (i+1);
            for (int j = 1; j <= receivers; j++) {
                data[i][j] = "0";
            }
            data[i][receivers + 1] = "0";
            data[i][receivers + 2] = "0";
        }

        data[suppliers][0] = "Popyt";

        for (int j = 1; j <= receivers; j++) {
            data[suppliers][j] = "0";
        }
        data[suppliers][receivers + 1] = "";
        data[suppliers][receivers + 2] = "";

        data[suppliers + 1][0] = "Cena sprzedaży";

        for (int j = 1; j <= receivers; j++) {
            data[suppliers + 1][j] = "0";
        }

        data[suppliers + 1][receivers + 1] = "";
        data[suppliers + 1][receivers + 2] = "";

        dataTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }

    private void calculate() {
        try {
            TransportProblem problem = readProblemFromTable();

            MaxElementSolver solver = new MaxElementSolver();
            TransportResult result = solver.solve(problem);

            showResult(result, problem);
        } catch (Exception e) {
            resultArea.setText("Błąd danych wejściowych. Sprawdź, czy wszystkie pola zawierają liczby.");
        }
    }

    private TransportProblem readProblemFromTable() {
        int suppliers = Integer.parseInt(suppliersField.getText());
        int receivers = Integer.parseInt(receiversField.getText());

        int[][] transportCosts = new int[suppliers][receivers];
        int[] supply = new int[suppliers];
        int[] demand = new int[receivers];
        int[] purchasePrices = new int[suppliers];
        int[] sellingPrices = new int[receivers];
        boolean[][] blocked = new boolean[suppliers][receivers];

        for (int i = 0; i < suppliers; i++) {
            for (int j = 0; j < receivers; j++) {
                transportCosts[i][j] = Integer.parseInt(dataTable.getValueAt(i, j + 1).toString());
            }

            supply[i] = Integer.parseInt(dataTable.getValueAt(i, receivers + 1).toString());
            purchasePrices[i] = Integer.parseInt(dataTable.getValueAt(i, receivers + 2).toString());
        }

        for (int j = 0; j < receivers; j++) {
            demand[j] = Integer.parseInt(dataTable.getValueAt(suppliers, j + 1).toString());
            sellingPrices[j] = Integer.parseInt(dataTable.getValueAt(suppliers + 1, j + 1).toString());
        }

        return new TransportProblem(transportCosts, supply, demand, purchasePrices, sellingPrices, blocked);
    }

    private void showResult(TransportResult result, TransportProblem problem) {
        StringBuilder text = new StringBuilder();

        int[][] profits = problem.calculateProfitMatrix();
        int[][] allocation = result.getAllocation();

        text.append("MACIERZ ZYSKU JEDNOSTKOWEGO:\n");

        for (int i = 0; i < profits.length; i++) {
            for (int j = 0; j < profits[i].length; j++) {
                text.append(profits[i][j]).append("\t");
            }
            text.append("\n");
        }

        text.append("\nITERACJE:\n");

        int counter = 1;

        for (IterationStep step : result.getSteps()) {
            text.append(counter)
                    .append(". D")
                    .append(step.getSupplier() + 1)
                    .append(" -> O")
                    .append(step.getReceiver() + 1)
                    .append(", zysk jedn.: ")
                    .append(step.getProfit())
                    .append(", przydział: ")
                    .append(step.getAmount())
                    .append("\n");

            counter++;
        }

        text.append("\nMACIERZ PRZYDZIAŁÓW:\n");

        for (int i = 0; i < allocation.length; i++) {
            for (int j = 0; j < allocation[i].length; j++) {
                text.append(allocation[i][j]).append("\t");
            }
            text.append("\n");
        }

        text.append("\nŁączny zysk: ").append(result.getTotalProfit());

        resultArea.setText(text.toString());
    }
}