package net.codejava.yahtzeegame.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 *Oyun bitiminde skoru ve kazan anı gösteren, ardından
 * kullanıcıya “Play Again” veya “Exit” seçenekleri sunan Swing paneli
 */
public class EndPanel extends JPanel {

     // ----------- Swing bileşenleri -----------
    private JLabel winnerLabel;                 // En üstte “Winner: …”
    private final JLabel titleLabel  = new JLabel("Game Over", SwingConstants.CENTER);
    private final JTextArea scoreArea = new JTextArea(10, 30);   // Çoklu satır skor listesi
    private final JButton replayButton = new JButton("Play Again");
    private final JButton exitButton   = new JButton("Exit");

    /** Kurucu, panel yerleşimini ve görsel ayarları yapar. */
    public EndPanel() {
        setLayout(new BorderLayout(10, 10));

        // ----- Başlık -----
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        add(titleLabel, BorderLayout.NORTH);

        // ----- Kazanan etiketi -----
        winnerLabel = new JLabel("Winner: ");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // ----- Skor alanı  -----
        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Monospaced", Font.PLAIN, 18));

        // Center bölgesi: kazanan + skor listesi
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(winnerLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(scoreArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Alt buton çubuğu
        JPanel btnPanel = new JPanel();
        btnPanel.add(replayButton);
        btnPanel.add(exitButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Paneli günceller ve oyun sonuçlarını kullanıcıya gösterir.
     */
    public void showResults(Map<String, Integer> results, String winner) {
        winnerLabel.setText("Winner: " + winner);

        // Skor tablosunu metin olarak oluştur
        StringBuilder sb = new StringBuilder("Scores:\n");
        results.forEach((player, score) -> sb.append(String.format("%-12s : %3d%n", player, score)));

        if (winner.equalsIgnoreCase("Draw")) {
            sb.append("\nResult: Draw!");
        }
        scoreArea.setText(sb.toString());
    }

    public void setReplayAction(ActionListener listener) {
        for (ActionListener l : replayButton.getActionListeners())
            replayButton.removeActionListener(l);
        replayButton.addActionListener(listener);
    }

    public void setExitAction(ActionListener listener) {
        for (ActionListener l : exitButton.getActionListeners())
            exitButton.removeActionListener(l);
        exitButton.addActionListener(listener);
    }
}
