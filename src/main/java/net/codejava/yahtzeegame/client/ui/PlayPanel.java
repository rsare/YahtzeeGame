package net.codejava.yahtzeegame.client.ui;

import net.codejava.yahtzeegame.client.model.ScoreCategory;
import net.codejava.yahtzeegame.client.model.ScoreCalculator;
import net.codejava.yahtzeegame.network.Message;
import net.codejava.yahtzeegame.network.NetworkClient;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PlayPanel extends JPanel {
    private JLabel lblTurnInfo = new JLabel("Waiting for opponent...", SwingConstants.CENTER);
    private DicePanel[] dicePanels = new DicePanel[5];
    private JCheckBox[] holdBoxes = new JCheckBox[5];
    private JButton btnRoll = new JButton("Roll Dice");
    private JButton btnSurrender = new JButton("Surrender");
    private JPanel categoriesPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    private ScoreBoardPanel scoreBoardPanel;


    private NetworkClient client;
    private String playerName, opponentName;
    private int[] dice = new int[5];
    private Map<ScoreCategory, JButton> categoryButtons = new LinkedHashMap<>();
    private int rollCount = 0;  // Roll sayısı

    public PlayPanel(NetworkClient client) {
        this.client = client;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        scoreBoardPanel = new ScoreBoardPanel(playerName, opponentName);


        // Zar ve Hold paneli
        JPanel zarVeHoldPanel = new JPanel();
        zarVeHoldPanel.setLayout(new BoxLayout(zarVeHoldPanel, BoxLayout.Y_AXIS));
        JPanel zarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JPanel holdPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        for (int i = 0; i < 5; i++) {
            dicePanels[i] = new DicePanel();
            zarPanel.add(dicePanels[i]);
            holdBoxes[i] = new JCheckBox("Hold");
            holdBoxes[i].setEnabled(false); // Başlangıçta kapalı
            holdPanel.add(holdBoxes[i]);
        }
        zarVeHoldPanel.add(zarPanel);
        zarVeHoldPanel.add(holdPanel);
        scoreBoardPanel.setPlayerNames(playerName, opponentName);
        scoreBoardPanel = new ScoreBoardPanel(playerName, opponentName);


        add(zarVeHoldPanel, BorderLayout.NORTH);

        // Kategori butonları
        for (ScoreCategory cat : ScoreCategory.values()) {
            JButton btn = new JButton(cat.displayName());
            btn.setEnabled(false);
            btn.addActionListener(e -> chooseCategory(cat));
            categoryButtons.put(cat, btn);
            categoriesPanel.add(btn);
        }
        categoriesPanel.setBorder(BorderFactory.createTitledBorder("Select Category"));
        add(categoriesPanel, BorderLayout.EAST);

        // Skor tablosu
        scoreBoardPanel = new ScoreBoardPanel(playerName, opponentName);
        add(scoreBoardPanel, BorderLayout.WEST);

        // Kontrol butonları paneli
        JPanel controlPanel = new JPanel();
        controlPanel.add(btnRoll);
        controlPanel.add(btnSurrender);
        add(controlPanel, BorderLayout.SOUTH);

        // Buton aksiyonları
        btnRoll.addActionListener(e -> {
            if (rollCount >= 3) {
                JOptionPane.showMessageDialog(this, "You have no more rolls left. Please select a category.");
                return;
            }
            sendRollRequest();
        });

        btnSurrender.addActionListener(e -> sendSurrender());

        setAllDiceAndHold(1, false);
        updateControlsForNewTurn();
    }

    public void initForNewGame(String playerName, String opponentName) {
        this.playerName = playerName;
        this.opponentName = opponentName;
        //lblTurnInfo.setText("You: " + playerName + " | Opponent: " + opponentName);
        lblTurnInfo.setText("Game started! Your opponent: " + opponentName);
        setAllDiceAndHold(1, false);
        enableAllCategoryButtons(false);
        updateControlsForNewTurn();
    }

    public void updateTurn(Message msg) {
        String turnPlayer = (String) msg.get("currentPlayer");


        @SuppressWarnings("unchecked")
        List<Integer> diceList = (List<Integer>) msg.get("dice");
        int[] diceArray = diceList.stream().mapToInt(Integer::intValue).toArray();

        @SuppressWarnings("unchecked")
        Map<ScoreCategory, Integer> p1Scores = (Map<ScoreCategory, Integer>) msg.get("p1Scores");
        @SuppressWarnings("unchecked")
        Map<ScoreCategory, Integer> p2Scores = (Map<ScoreCategory, Integer>) msg.get("p2Scores");

        if (playerName.equals(msg.get("playerName"))) {
            scoreBoardPanel.updateScores(p1Scores, p2Scores);
        } else {
            // oyuncu2 isek, skorları ters geç
            scoreBoardPanel.updateScores(p2Scores, p1Scores);
        }

        Object usedCatsObj = msg.get("usedCategories");
        boolean[] usedCats;

        if (usedCatsObj instanceof boolean[]) {
            usedCats = (boolean[]) usedCatsObj;
        } else if (usedCatsObj instanceof Boolean[]) {
            Boolean[] boxed = (Boolean[]) usedCatsObj;
            usedCats = new boolean[boxed.length];
            for (int i = 0; i < boxed.length; i++) {
                usedCats[i] = boxed[i] != null && boxed[i];
            }
        } else {
            usedCats = new boolean[ScoreCategory.values().length];
        }

        rollCount = msg.get("rollCount") instanceof Integer ? (Integer) msg.get("rollCount") : 0;

        // Zarları güncelle
        System.arraycopy(diceArray, 0, dice, 0, dice.length);
        for (int i = 0; i < dicePanels.length; i++) {
            dicePanels[i].setValue(dice[i]);
            dicePanels[i].repaint();
        }

        // Kategori butonlarını aktif/pasif et
        ScoreCategory[] cats = ScoreCategory.values();
        for (int i = 0; i < cats.length; i++) {
            categoryButtons.get(cats[i]).setEnabled(
                    turnPlayer.equals(playerName) && !usedCats[i] && rollCount > 0
            );
        }

        // Roll ve hold butonlarını kontrol et
        boolean isMyTurn = turnPlayer.equals(playerName);
        btnRoll.setEnabled(isMyTurn && rollCount < 3);

        // Hold kutuları sadece 1 veya 2. roll'da aktif olsun
        boolean enableHolds = isMyTurn && rollCount > 0 && rollCount < 3;
        for (JCheckBox box : holdBoxes) {
            box.setEnabled(enableHolds);
            if (!box.isEnabled()) box.setSelected(false);
        }

        // Turn info güncelle
        if (isMyTurn) {
            lblTurnInfo.setText("Your turn! (" + rollCount + "/3 rolls)");
        } else {
            lblTurnInfo.setText(opponentName + "'s turn...");
        }

        // Skor tablosunu güncelle
        scoreBoardPanel.updateScores(p1Scores, p2Scores);

        // Eğer rollCount == 3 ise Roll Dice devre dışı, Category seçimi aktif olmalı
        if (rollCount == 3 && isMyTurn) {
            btnRoll.setEnabled(false);
            enableAllCategoryButtons(true);
        }
    }

    private void sendRollRequest() {
        try {
            Message msg = new Message("ROLL_REQUEST");
            List<Boolean> holds = new ArrayList<>();
            for (JCheckBox box : holdBoxes) {
                holds.add(box.isSelected());
            }
            msg.put("holds", holds);
            client.sendMessage(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendSurrender() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to surrender?",
                "Surrender",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                Message msg = new Message("SURRENDER");
                client.sendMessage(msg);

                // Butonları kapat
                btnSurrender.setEnabled(false);
                btnRoll.setEnabled(false);
                enableAllCategoryButtons(false);
                for (JCheckBox box : holdBoxes) {
                    box.setEnabled(false);
                    box.setSelected(false);
                }

                lblTurnInfo.setText("You surrendered. Waiting for game to end...");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error sending surrender: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }



    private void chooseCategory(ScoreCategory cat) {
        int score = ScoreCalculator.calculate(cat, dice);
        int r = JOptionPane.showConfirmDialog(this,
                String.format("You scored %d points for %s. Confirm?", score, cat.displayName()),
                "Confirm Category", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            try {
                Message msg = new Message("CATEGORY_CHOICE");
                msg.put("category", cat.name());
                client.sendMessage(msg);
                enableAllCategoryButtons(false);
                btnRoll.setEnabled(false);
                for (JCheckBox box : holdBoxes) box.setEnabled(false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to send category: " + ex.getMessage());
            }
        }
    }

    private void setAllDiceAndHold(int value, boolean hold) {
        for (int i = 0; i < 5; i++) {
            dicePanels[i].setValue(value);
            holdBoxes[i].setSelected(hold);
        }
    }

    private void enableAllCategoryButtons(boolean enable) {
        for (JButton btn : categoryButtons.values()) btn.setEnabled(enable);
    }

    public void disableGameControls() {
        btnRoll.setEnabled(false);
        btnSurrender.setEnabled(false);
        for (JCheckBox box : holdBoxes) box.setEnabled(false);
        for (JButton btn : categoryButtons.values()) btn.setEnabled(false);
    }

    private void updateControlsForNewTurn() {
        rollCount = 0;
        btnRoll.setEnabled(true);
        enableAllCategoryButtons(false);
        for (JCheckBox box : holdBoxes) {
            box.setEnabled(false);
            box.setSelected(false);
        }
        setAllDiceAndHold(1, false);
        lblTurnInfo.setText("Waiting for your turn...");
    }
}
