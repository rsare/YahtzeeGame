
package net.codejava.yahtzeegame.client.ui;

import net.codejava.yahtzeegame.client.model.ScoreCategory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;

public class ScoreBoardPanel extends JPanel {
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private String name;

    // Kategori adlarını tek seferde çekiyorum
    private static final String[] CATEGORY_NAMES = {
            "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes",
            "Three of a Kind", "Four of a Kind", "Full House",
            "Small Straight", "Large Straight", "Yahtzee", "Chance"
    };

    public ScoreBoardPanel(String player1, String player2) {
        setLayout(new BorderLayout(5,5));
        setBorder(BorderFactory.createTitledBorder("Score Board"));


        String[] columns = {"Category", player1, player2};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (String cat : CATEGORY_NAMES) {
            tableModel.addRow(new Object[]{cat, "-", "-"});
        }

        scoreTable = new JTable(tableModel);
        scoreTable.setRowHeight(26);

        add(new JScrollPane(scoreTable), BorderLayout.CENTER);
    }




    //Skorları günceller. Map: kategori adı (enum) -> skor

    public void updateScores(Map<ScoreCategory, Integer> p1Scores, Map<ScoreCategory, Integer> p2Scores) {
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            ScoreCategory cat = ScoreCategory.values()[i];
            tableModel.setValueAt(p1Scores.getOrDefault(cat, 0), i, 1);
            tableModel.setValueAt(p2Scores.getOrDefault(cat, 0), i, 2);
        }
    }
    public void setPlayerNames(String player1, String player2) {
        String[] newColumns = {"Category", player1, player2};
        tableModel.setColumnIdentifiers(newColumns);
    }



     //Oyun bitiminde toplam skorları gösterir

    public void showTotals(int p1Total, int p2Total) {
        tableModel.addRow(new Object[]{"TOTAL", p1Total, p2Total});
    }
}
