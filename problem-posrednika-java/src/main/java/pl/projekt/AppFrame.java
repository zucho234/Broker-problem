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

        JPanel topPanel = new JPanel(new GridLayout(2,2,10,10));
        topPanel.add(new JLabel("Liczba dostawców:"));
        topPanel.add(suppliersField);
        topPanel.add(new JLabel("Liczba odbiorców:"));
        topPanel.add(receiversField);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(topPanel, BorderLayout.CENTER);
        northPanel.add(createTableButton, BorderLayout.EAST);

        dataTable = new JTable();

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setText("Tutaj pojawi się wynik działania programu.");

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);
        add(new JScrollPane(resultArea), BorderLayout.SOUTH);

        createTable();

        //JFrame frame = new AppFrame();
        //frame.setSize(1000,800);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setLocationRelativeTo(null);
        //frame.setVisible(true);
    }

    private void createTable() {
        int suppliers = Integer.parseInt(suppliersField.getText());
        int receivers = Integer.parseInt(receiversField.getText());

        String[] columns = new String[receivers+2];
        columns[0] = "";
        for (int i = 1; i <= receivers; i++) {
            columns[i] = "0" + i;
        }
        columns[receivers + 1] = "Podaż";

        String[][] data = new String[suppliers + 1][receivers + 2];

        for (int i = 0; i < suppliers; i++) {
            data[i][0] = "D" + (i+1);
            for (int j = 1; j <= receivers; j++) {
                data[i][j] = "0";
            }
            data[i][receivers + 1] = "0";
        }

        data[suppliers][0] = "Popyt";
        for (int j = 1; j <= receivers; j++) {
            data[suppliers][j] = "0";
        }
        data[suppliers][receivers + 1] = "";

        dataTable.setModel(new javax.swing.table.DefaultTableModel(data, columns));
    }
}