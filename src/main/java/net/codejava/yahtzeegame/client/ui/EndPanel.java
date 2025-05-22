// ================================================
// FILE: src/main/java/net/codejava/yahtzeegame/client/ui/EndPanel.java
// ================================================
package net.codejava.yahtzeegame.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Oyun sonu skor tablosu ve tekrar oynama/çıkış için panel.
 */
public class EndPanel extends JPanel {

    private JLabel winnerLabel;
    // private JTextArea ScoreArea; // Kullanılmayan değişken kaldırıldı
    private JLabel titleLabel = new JLabel("Game Over", SwingConstants.CENTER);
    private JTextArea scoreArea = new JTextArea(10, 30); // Bu değişken kullanılıyor
    private JButton replayButton = new JButton("Play Again");
    private JButton exitButton = new JButton("Exit");

    public EndPanel() {
        setLayout(new BorderLayout(10, 10));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        add(titleLabel, BorderLayout.NORTH);

        winnerLabel = new JLabel("Winner: ");
        winnerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        winnerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        scoreArea.setEditable(false);
        scoreArea.setFont(new Font("Monospaced", Font.PLAIN, 18));

        JPanel centerPanel = new JPanel(new BorderLayout(5,5));
        centerPanel.add(winnerLabel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(scoreArea), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        btnPanel.add(replayButton);
        btnPanel.add(exitButton);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * Skorları ve kazananı gösterir.
     * @param results Skor tablosu (oyuncu adı -> toplam skor)
     * @param winner Kazanan oyuncu adı veya "Draw" (Beraberlik)
     */
    public void showResults(Map<String, Integer> results, String winner) {
        winnerLabel.setText("Winner: " + winner);
        StringBuilder sb = new StringBuilder();
        sb.append("Scores:\n");
        results.forEach((player, score) -> sb.append(String.format("%-12s : %3d\n", player, score)));
        sb.append("\n");
        if (winner.equalsIgnoreCase("Draw")) {
            sb.append("Result: Draw!");
        } else {
            // Winner bilgisi zaten winnerLabel'da var, tekrar eklemeye gerek yok veya isteğe bağlı
            // sb.append("Winner: ").append(winner);
        }
        scoreArea.setText(sb.toString());
    }

    public void setReplayAction(ActionListener listener) {
        for (ActionListener l : replayButton.getActionListeners()) {
            replayButton.removeActionListener(l);
        }
        replayButton.addActionListener(listener);
    }

    public void setExitAction(ActionListener listener) {
        for (ActionListener l : exitButton.getActionListeners()) {
            exitButton.removeActionListener(l);
        }
        exitButton.addActionListener(listener);
    }
}