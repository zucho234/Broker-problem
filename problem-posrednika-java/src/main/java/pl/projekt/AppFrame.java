package pl.projekt;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class AppFrame extends JFrame {
    private static final int MAX_PARTICIPANTS = 10;

    private JTextField suppliersField;
    private JTextField receiversField;
    private JTable dataTable;
    private JTextArea resultArea;
    private GraphPanel graphPanel;
    private JLabel iterationLabel;
    private JButton previousButton;
    private JButton nextButton;

    private TransportResult currentResult;
    private int currentStepIndex = -1;

    public AppFrame() {
        setTitle("Problem pośrednika");
        setSize(1250, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        suppliersField = new JTextField("3", 4);
        receiversField = new JTextField("3", 4);

        JButton createTableButton = new JButton("Utwórz tabelę");
        createTableButton.addActionListener(e -> createTable());

        JButton calculateButton = new JButton("Oblicz");
        calculateButton.addActionListener(e -> calculate());

        previousButton = new JButton("Poprzednia iteracja");
        previousButton.addActionListener(e -> changeStep(-1));

        nextButton = new JButton("Następna iteracja");
        nextButton.addActionListener(e -> changeStep(1));

        iterationLabel = new JLabel("Iteracja: -");

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        inputPanel.add(new JLabel("Liczba dostawców:"));
        inputPanel.add(suppliersField);
        inputPanel.add(new JLabel("Liczba odbiorców:"));
        inputPanel.add(receiversField);
        inputPanel.add(createTableButton);
        inputPanel.add(calculateButton);

        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        navigationPanel.add(previousButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(iterationLabel);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(inputPanel, BorderLayout.WEST);
        northPanel.add(navigationPanel, BorderLayout.EAST);

        dataTable = new JTable();
        dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataTable.setRowHeight(28);
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dataTable.setToolTipText("Wpisz X w komórce kosztu, aby zablokować trasę.");

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setRows(12);
        resultArea.setText("Wprowadź koszty transportu, podaż, popyt oraz ceny. W komórce trasy można wpisać X, aby ją zablokować.");

        graphPanel = new GraphPanel();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Dane wejściowe"));
        tablePanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Wyniki obliczeń"));
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        JPanel graphWrapper = new JPanel(new BorderLayout());
        graphWrapper.setBorder(BorderFactory.createTitledBorder("Graf iteracji"));
        graphWrapper.add(graphPanel, BorderLayout.CENTER);

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePanel, resultPanel);
        leftSplit.setResizeWeight(0.58);
        leftSplit.setDividerLocation(410);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, graphWrapper);
        mainSplit.setResizeWeight(0.54);
        mainSplit.setDividerLocation(650);

        add(northPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);

        createTable();
        updateNavigation();
    }

    private void createTable() {
        try {
            int suppliers = parseCount(suppliersField.getText(), "dostawców");
            int receivers = parseCount(receiversField.getText(), "odbiorców");

            String[] columns = new String[receivers + 3];
            columns[0] = "";
            for (int i = 1; i <= receivers; i++) {
                columns[i] = "O" + i;
            }
            columns[receivers + 1] = "Podaż";
            columns[receivers + 2] = "Cena zakupu";

            String[][] data = new String[suppliers + 2][receivers + 3];

            for (int i = 0; i < suppliers; i++) {
                data[i][0] = "D" + (i + 1);
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

            dataTable.setModel(new DefaultTableModel(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column != 0;
                }
            });

            for (int i = 0; i < columns.length; i++) {
                dataTable.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 115 : 92);
            }

            currentResult = null;
            currentStepIndex = -1;
            graphPanel.showStep(null, null);
            updateNavigation();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void calculate() {
        try {
            stopTableEditing();
            TransportProblem problem = readProblemFromTable();
            MaxElementSolver solver = new MaxElementSolver();
            currentResult = solver.solve(problem);

            if (currentResult.getSteps().isEmpty()) {
                currentStepIndex = -1;
                graphPanel.showStep(currentResult.getProblem(), null);
            } else {
                currentStepIndex = 0;
                graphPanel.showStep(currentResult.getProblem(), currentResult.getSteps().get(currentStepIndex));
            }

            showResult(currentResult);
            updateNavigation();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Błąd danych wejściowych. Sprawdź, czy wszystkie pola zawierają liczby albo X dla blokady trasy.");
        }
    }

    private TransportProblem readProblemFromTable() {
        int suppliers = parseCount(suppliersField.getText(), "dostawców");
        int receivers = parseCount(receiversField.getText(), "odbiorców");

        int[][] transportCosts = new int[suppliers][receivers];
        int[] supply = new int[suppliers];
        int[] demand = new int[receivers];
        int[] purchasePrices = new int[suppliers];
        int[] sellingPrices = new int[receivers];
        boolean[][] blocked = new boolean[suppliers][receivers];

        for (int i = 0; i < suppliers; i++) {
            for (int j = 0; j < receivers; j++) {
                String value = cellValue(i, j + 1);
                if (isBlockedValue(value)) {
                    blocked[i][j] = true;
                    transportCosts[i][j] = 0;
                } else {
                    transportCosts[i][j] = parseNonNegativeInt(value, "koszt transportu D" + (i + 1) + " -> O" + (j + 1));
                }
            }

            supply[i] = parseNonNegativeInt(cellValue(i, receivers + 1), "podaż D" + (i + 1));
            purchasePrices[i] = parseNonNegativeInt(cellValue(i, receivers + 2), "cena zakupu D" + (i + 1));
        }

        for (int j = 0; j < receivers; j++) {
            demand[j] = parseNonNegativeInt(cellValue(suppliers, j + 1), "popyt O" + (j + 1));
            sellingPrices[j] = parseNonNegativeInt(cellValue(suppliers + 1, j + 1), "cena sprzedaży O" + (j + 1));
        }

        if (sum(supply) == 0 || sum(demand) == 0) {
            throw new IllegalArgumentException("Suma podaży i suma popytu muszą być większe od zera.");
        }

        return new TransportProblem(transportCosts, supply, demand, purchasePrices, sellingPrices, blocked);
    }

    private void showResult(TransportResult result) {
        StringBuilder text = new StringBuilder();
        TransportProblem problem = result.getProblem();
        int[][] profits = problem.calculateProfitMatrix();
        int[][] allocation = result.getAllocation();

        text.append(result.getMessage()).append("\n\n");
        appendBalanceInfo(text, problem);

        text.append("MACIERZ ZYSKU JEDNOSTKOWEGO:\n");
        appendHeader(text, problem);
        for (int i = 0; i < profits.length; i++) {
            text.append(supplierName(problem, i)).append("\t");
            for (int j = 0; j < profits[i].length; j++) {
                text.append(problem.getBlocked()[i][j] ? "X" : profits[i][j]).append("\t");
            }
            text.append("\n");
        }

        text.append("\nITERACJE METODY MAKSYMALNEGO ELEMENTU:\n");
        int counter = 1;
        for (IterationStep step : result.getSteps()) {
            text.append(counter)
                    .append(". ")
                    .append(supplierName(problem, step.getSupplier()))
                    .append(" -> ")
                    .append(receiverName(problem, step.getReceiver()))
                    .append(", zysk jedn.: ")
                    .append(step.getProfit())
                    .append(", przydział: ")
                    .append(step.getAmount())
                    .append("\n");
            counter++;
        }

        text.append("\nMACIERZ PRZYDZIAŁÓW:\n");
        appendHeader(text, problem);
        for (int i = 0; i < allocation.length; i++) {
            text.append(supplierName(problem, i)).append("\t");
            for (int j = 0; j < allocation[i].length; j++) {
                text.append(allocation[i][j]).append("\t");
            }
            text.append("\n");
        }

        text.append("\nŁączny zysk: ").append(result.getTotalProfit());
        appendFinancialSummary(text, problem, allocation);
        if (!result.isFeasible()) {
            text.append("\n\nPozostały niezrealizowany popyt/podaż wynika najczęściej ze zbyt wielu blokad tras.");
        }

        resultArea.setText(text.toString());
        resultArea.setCaretPosition(0);
    }

    private void appendBalanceInfo(StringBuilder text, TransportProblem problem) {
        boolean balancedWithFake = false;
        for (boolean fake : problem.getFakeSuppliers()) {
            balancedWithFake = balancedWithFake || fake;
        }
        for (boolean fake : problem.getFakeReceivers()) {
            balancedWithFake = balancedWithFake || fake;
        }

        if (balancedWithFake) {
            text.append("Problem został zbilansowany przez dodanie fikcyjnego dostawcy Fd i fikcyjnego odbiorcy Fo.\n\n");
        } else {
            text.append("Problem był zbilansowany bez dodawania fikcyjnych uczestników.\n\n");
        }
    }

    private void appendFinancialSummary(StringBuilder text, TransportProblem problem, int[][] allocation) {
        int revenue = 0;
        int transportCost = 0;
        int purchaseCost = 0;

        int[][] transportCosts = problem.getTransportCosts();
        int[] purchasePrices = problem.getPurchasePrices();
        int[] sellingPrices = problem.getSellingPrices();

        for (int i = 0; i < problem.getSupplierCount(); i++) {
            for (int j = 0; j < problem.getReceiverCount(); j++) {
                if (!problem.isFakeSupplier(i) && !problem.isFakeReceiver(j)) {
                    int amount = allocation[i][j];
                    revenue += amount * sellingPrices[j];
                    transportCost += amount * transportCosts[i][j];
                    purchaseCost += amount * purchasePrices[i];
                }
            }
        }

        text.append("\n\nPRZYCHODY I KOSZTY DLA RZECZYWISTYCH TRAS:");
        text.append("\nPrzychód ze sprzedaży: ").append(revenue);
        text.append("\nKoszt transportu: ").append(transportCost);
        text.append("\nKoszt zakupu: ").append(purchaseCost);
        text.append("\nZysk = przychód - transport - zakup: ")
                .append(revenue)
                .append(" - ")
                .append(transportCost)
                .append(" - ")
                .append(purchaseCost)
                .append(" = ")
                .append(revenue - transportCost - purchaseCost);
    }

    private void appendHeader(StringBuilder text, TransportProblem problem) {
        text.append("\t");
        for (int j = 0; j < problem.getReceiverCount(); j++) {
            text.append(receiverName(problem, j)).append("\t");
        }
        text.append("\n");
    }

    private void changeStep(int direction) {
        if (currentResult == null || currentResult.getSteps().isEmpty()) {
            return;
        }

        int nextIndex = currentStepIndex + direction;
        if (nextIndex < 0 || nextIndex >= currentResult.getSteps().size()) {
            return;
        }

        currentStepIndex = nextIndex;
        graphPanel.showStep(currentResult.getProblem(), currentResult.getSteps().get(currentStepIndex));
        updateNavigation();
    }

    private void updateNavigation() {
        boolean hasSteps = currentResult != null && !currentResult.getSteps().isEmpty();
        previousButton.setEnabled(hasSteps && currentStepIndex > 0);
        nextButton.setEnabled(hasSteps && currentStepIndex < currentResult.getSteps().size() - 1);

        if (hasSteps) {
            iterationLabel.setText("Iteracja: " + (currentStepIndex + 1) + "/" + currentResult.getSteps().size());
        } else {
            iterationLabel.setText("Iteracja: -");
        }
    }

    private int parseCount(String value, String label) {
        int count = parseNonNegativeInt(value, "liczba " + label);
        if (count < 1 || count > MAX_PARTICIPANTS) {
            throw new IllegalArgumentException("Liczba " + label + " musi być z zakresu 1-" + MAX_PARTICIPANTS + ".");
        }
        return count;
    }

    private int parseNonNegativeInt(String value, String label) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new IllegalArgumentException("Pole '" + label + "' nie może być ujemne.");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Pole '" + label + "' musi zawierać liczbę całkowitą.");
        }
    }

    private String cellValue(int row, int column) {
        Object value = dataTable.getValueAt(row, column);
        return value == null ? "" : value.toString().trim();
    }

    private void stopTableEditing() {
        if (dataTable.isEditing()) {
            TableCellEditor editor = dataTable.getCellEditor();
            if (editor != null) {
                editor.stopCellEditing();
            }
        }
    }

    private boolean isBlockedValue(String value) {
        return "X".equalsIgnoreCase(value.trim());
    }

    private int sum(int[] values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return sum;
    }

    private String supplierName(TransportProblem problem, int index) {
        return problem.isFakeSupplier(index) ? "Fd" : "D" + (index + 1);
    }

    private String receiverName(TransportProblem problem, int index) {
        return problem.isFakeReceiver(index) ? "Fo" : "O" + (index + 1);
    }

    private void showError(String message) {
        resultArea.setText(message);
        JOptionPane.showMessageDialog(this, message, "Niepoprawne dane", JOptionPane.ERROR_MESSAGE);
    }
}
