package pl.projekt;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> {
            AppFrame app = new AppFrame();
            app.setVisible(true);
        });
    }
}
