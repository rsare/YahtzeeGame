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

    public PlayPanel(NetworkClient client) {
        this.client = client;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Zar ve Hold paneli
        JPanel zarVeHoldPanel = new JPanel();
        zarVeHoldPanel.setLayout(new BoxLayout(zarVeHoldPanel, BoxLayout.Y_AXIS));
        JPanel zarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JPanel holdPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        for (int i = 0; i < 5; i++) {
            dicePanels[i] = new DicePanel();
            zarPanel.add(dicePanels[i]);
            holdBoxes[i] = new JCheckBox("Hold");
            holdPanel.add(holdBoxes[i]);
        }
        zarVeHoldPanel.add(zarPanel);
        zarVeHoldPanel.add(holdPanel);

        JPanel diceContainer = new JPanel(new FlowLayout());
        for (int i = 0; i < 5; i++) {
            dicePanels[i] = new DicePanel();
            diceContainer.add(dicePanels[i]);
        }
        add(diceContainer); // veya uygun yere eklejhjgjhhjj

        // Kategori butonları
        for (ScoreCategory cat : ScoreCategory.values()) {
            JButton btn = new JButton(cat.displayName());
            btn.setEnabled(false);
            btn.addActionListener(e -> chooseCategory(cat));
            categoryButtons.put(cat, btn);
            categoriesPanel.add(btn);
        }
        categoriesPanel.setBorder(BorderFactory.createTitledBorder("Select Category"));

        // Skor tablosu
        scoreBoardPanel = new ScoreBoardPanel("You", "Opponent");

        // Layout
        add(lblTurnInfo, BorderLayout.PAGE_START);
        add(zarVeHoldPanel, BorderLayout.NORTH);
        add(categoriesPanel, BorderLayout.EAST);
        add(scoreBoardPanel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel();
        controlPanel.add(btnRoll);
        controlPanel.add(btnSurrender);
        add(controlPanel, BorderLayout.SOUTH);

        // Buton aksiyonları
        btnRoll.addActionListener(e -> sendRollRequest());
        btnSurrender.addActionListener(e -> sendSurrender());

        setAllDiceAndHold(1, false);
    }

    public void initForNewGame(String playerName, String opponentName) {
        this.playerName = playerName;
        this.opponentName = opponentName;
        lblTurnInfo.setText("Game started! Your opponent: " + opponentName);
        setAllDiceAndHold(1, false);
        enableAllCategoryButtons(false);
    }

    public void updateTurn(Message msg) {
        String turnPlayer = (String) msg.get("currentPlayer");

        @SuppressWarnings("unchecked")
        List<Integer> diceList = (List<Integer>) msg.get("dice");
        int[] diceArray = diceList.stream().mapToInt(Integer::intValue).toArray();

        Map<ScoreCategory, Integer> p1Scores = (Map<ScoreCategory, Integer>) msg.get("p1Scores");
        Map<ScoreCategory, Integer> p2Scores = (Map<ScoreCategory, Integer>) msg.get("p2Scores");

        // usedCategories nesnesini al ve türüne göre boolean[]'e çevir
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
            // Eğer tip beklenmedikse, false dizisi oluştur (hepsi boş)
            usedCats = new boolean[ScoreCategory.values().length];
        }

        int currentRoll = msg.get("rollCount") instanceof Integer ? (Integer) msg.get("rollCount") : 0;

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
                    turnPlayer.equals(playerName) && !usedCats[i] && currentRoll > 0
            );
        }

        // Roll ve hold butonlarını kontrol et
        boolean isMyTurn = turnPlayer.equals(playerName);
        btnRoll.setEnabled(isMyTurn && currentRoll < 3);

        for (JCheckBox box : holdBoxes) {
            box.setEnabled(isMyTurn && currentRoll > 0 && currentRoll < 3);
            if (!box.isEnabled()) box.setSelected(false);
        }

        // Turn info güncelle
        if (isMyTurn) {
            lblTurnInfo.setText("Your turn! (" + (currentRoll + 1) + "/3)");
        } else {
            lblTurnInfo.setText(opponentName + "'s turn...");
        }

        // Skor tablosunu güncelle
        scoreBoardPanel.updateScores(p1Scores, p2Scores);
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

                // Butonu devre dışı bırak (çift tıklamayı önle)
                btnSurrender.setEnabled(false);

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

}