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

/**
 * PlayPanel – oyunun asıl oynandığı Swing paneli
 */
public class PlayPanel extends JPanel {

    // Swing bileşenleri
    private final JLabel lblTurnInfo = new JLabel("Waiting for opponent...", SwingConstants.CENTER);
    private final DicePanel[] dicePanels = new DicePanel[5];
    private final JCheckBox[] holdBoxes  = new JCheckBox[5];
    private final JButton btnRoll     = new JButton("Roll Dice");
    private final JButton btnSurrender = new JButton("Surrender");
    private final JPanel  categoriesPanel = new JPanel(new GridLayout(0, 2, 5, 5));
    private ScoreBoardPanel scoreBoardPanel;
    private boolean isPlayer1;    // ➜ Ben sunucudaki P1 miyim?


    //  Oyun durumu
    private final NetworkClient client;                     // TCP bağlantısı
    private String playerName, opponentName;                // Takma adlar
    private final int[] dice = new int[5];                  // Son zarlar
    private final Map<ScoreCategory, JButton> categoryButtons = new LinkedHashMap<>();
    private int rollCount = 0;                              // Tur başına 0‒3 arası

    // Kurucu – UI bileşenlerini oluşturur ve aksiyonları tanımlar
    public PlayPanel(NetworkClient client) {
        this.client = client;

        // scoreBoardPanel = new ScoreBoardPanel("", "");
        scoreBoardPanel = new ScoreBoardPanel(playerName, opponentName);
        add(scoreBoardPanel, BorderLayout.WEST);


        // Panel ayarları
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setBackground(new Color(218, 193, 193)); // Pastel gri

        // scoreBoardPanel = new ScoreBoardPanel(playerName, opponentName); // Geçici

        // Üst bölge: Zarlar + Hold kutuları
        JPanel zarVeHoldPanel = new JPanel();
        zarVeHoldPanel.setLayout(new BoxLayout(zarVeHoldPanel, BoxLayout.Y_AXIS));
        JPanel zarPanel  = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JPanel holdPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        for (int i = 0; i < 5; i++) {
            dicePanels[i] = new DicePanel();
            zarPanel.add(dicePanels[i]);

            holdBoxes[i] = new JCheckBox("Hold");
            holdBoxes[i].setEnabled(false);  // Oyuncu zar atana kadar kapalı
            holdPanel.add(holdBoxes[i]);
        }
        zarVeHoldPanel.add(zarPanel);
        zarVeHoldPanel.add(holdPanel);
        add(zarVeHoldPanel, BorderLayout.NORTH);

        // Kategori butonları
        for (ScoreCategory cat : ScoreCategory.values()) {
            JButton btn = new JButton(cat.displayName());
            btn.setEnabled(false);                       // Tur oyuncusunda değilken kapalı
            btn.addActionListener(e -> chooseCategory(cat));
            categoryButtons.put(cat, btn);
            categoriesPanel.add(btn);
        }
        categoriesPanel.setBorder(BorderFactory.createTitledBorder("Select Category"));
        add(categoriesPanel, BorderLayout.EAST);

        //  Skor tablosu
        add(scoreBoardPanel, BorderLayout.WEST);

        //  Kontrol butonları
        JPanel controlPanel = new JPanel();
        controlPanel.add(btnRoll);
        controlPanel.add(btnSurrender);
        add(controlPanel, BorderLayout.SOUTH);

        // Buton aksiyonları
        btnRoll.addActionListener(e -> {
            if (rollCount >= 3) {
                JOptionPane.showMessageDialog(this,
                        "You have no more rolls left. Please select a category.");
                return;
            }
            sendRollRequest();
        });

        btnSurrender.addActionListener(e -> sendSurrender());

        // Başlangıç durumu
        setAllDiceAndHold(1, false);
        updateControlsForNewTurn();
    }

//       SUNUCUDAN GELEN MESAJLAR -> Görünümü güncelleme


    // Her yeni maçta skorları ve kontrolleri sıfırlar
    public void initForNewGame(String playerName, String opponentName, boolean amIP1) {
        this.playerName   = playerName;
        this.opponentName = opponentName;
        this.isPlayer1 = amIP1;
        scoreBoardPanel.setPlayerNames(playerName, opponentName);

        lblTurnInfo.setText("Game started! Your opponent: " + opponentName);
        setAllDiceAndHold(1, false);
        enableAllCategoryButtons(false);
        updateControlsForNewTurn();
    }


     // TURN_UPDATE mesajıyla çağrılır – zar değerleri, skorlar ve UI durumunu tazeler

    public void updateTurn(Message msg) {
        String turnPlayer = (String) msg.get("currentPlayer");

        // Zar dizisi
        @SuppressWarnings("unchecked")
        int[] diceArray = ((List<Integer>) msg.get("dice")).stream().mapToInt(Integer::intValue).toArray();

        // Skor tabloları
        @SuppressWarnings("unchecked")
        Map<ScoreCategory, Integer> p1Scores = (Map<ScoreCategory, Integer>) msg.get("p1Scores");
        @SuppressWarnings("unchecked")
        Map<ScoreCategory, Integer> p2Scores = (Map<ScoreCategory, Integer>) msg.get("p2Scores");

        // Ekrandaki tabloyu hangi sırayla güncelleyeceğimizi belirle
        if (playerName.equals(msg.get("playerName"))) {
            scoreBoardPanel.updateScores(p1Scores, p2Scores);
        } else {
            scoreBoardPanel.updateScores(p2Scores, p1Scores);
        }

        // Hangi kategorilerin dolu olduğu bilgisi
        boolean[] usedCats = extractUsedCats(msg.get("usedCategories"));

        // Roll sayısı
        rollCount = msg.get("rollCount") instanceof Integer ? (Integer) msg.get("rollCount") : 0;

        // Zar panellerini boyat
        System.arraycopy(diceArray, 0, dice, 0, dice.length);
        for (int i = 0; i < dicePanels.length; i++) {
            dicePanels[i].setValue(dice[i]);
            dicePanels[i].repaint();
        }

        // Kategori butonlarının aktifliği
        ScoreCategory[] cats = ScoreCategory.values();
        for (int i = 0; i < cats.length; i++) {
            categoryButtons.get(cats[i]).setEnabled(
                    turnPlayer.equals(playerName) && !usedCats[i] && rollCount > 0);
        }

        // Roll & Hold kontrolleri
        boolean isMyTurn = turnPlayer.equals(playerName);
        btnRoll.setEnabled(isMyTurn && rollCount < 3);

        boolean enableHolds = isMyTurn && rollCount > 0 && rollCount < 4;
        for (JCheckBox box : holdBoxes) {
            box.setEnabled(enableHolds);
            if (!box.isEnabled()) box.setSelected(false);
        }

        // Tur bilgisi etiketi
        lblTurnInfo.setText(isMyTurn
                ? "Your turn! (" + rollCount + "/3 rolls)"
                : opponentName + "'s turn...");

        // Eğer 3 atış dolduysa kategori seçimine zorla
        if (rollCount == 3 && isMyTurn) {
            btnRoll.setEnabled(false);
            enableAllCategoryButtons(true);
        }
    }

    //KULLANICI EYLEMLERİ -> Sunucuya mesaj gönderme

    // Roll Dice -> ROLL_REQUEST mesajı gönderir
    private void sendRollRequest() {
        try {
            Message msg = new Message("ROLL_REQUEST");
            List<Boolean> holds = new ArrayList<>();
            for (JCheckBox box : holdBoxes) holds.add(box.isSelected());
            msg.put("holds", holds);
            client.sendMessage(msg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Surrender butonu -> Kullanıcı onaylarsa SURRENDER mesajı
    private void sendSurrender() {
        int choice = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to surrender?",
                "Surrender", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                Message msg = new Message("SURRENDER");
                client.sendMessage(msg);
                btnSurrender.setEnabled(false);  // Panel devre dışı
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error sending surrender: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    //Seçilen kategori ve hesaplanan skoru sunucuya yollar
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
                JOptionPane.showMessageDialog(this,
                        "Failed to send category: " + ex.getMessage());
            }
        }
    }

       //YARDIMCI METOTLAR

    // Zar yüzlerini ve Hold kutularını topluca ayarlar
    private void setAllDiceAndHold(int value, boolean hold) {
        for (int i = 0; i < 5; i++) {
            dicePanels[i].setValue(value);
            holdBoxes[i].setSelected(hold);
        }
    }

    // Tüm kategori butonlarını aktif/pasif yapar
    private void enableAllCategoryButtons(boolean enable) {
        categoryButtons.values().forEach(btn -> btn.setEnabled(enable));
    }

    //Oyun bittiğinde veya karşıdan GAME_OVER geldiğinde kontrolleri kilitler
    public void disableGameControls() {
        btnRoll.setEnabled(false);
        btnSurrender.setEnabled(false);
        for (JCheckBox box : holdBoxes) box.setEnabled(false);
        for (JButton btn : categoryButtons.values()) btn.setEnabled(false);
    }

    // Tur başında roll & hold eylemlerini sıfırlar
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

    // TURN_UPDATE içindeki usedCategories alanını güvenle boolean[]’e dönüştürür
    private boolean[] extractUsedCats(Object usedCatsObj) {
        if (usedCatsObj instanceof boolean[]) {
            return (boolean[]) usedCatsObj;
        }
        if (usedCatsObj instanceof Boolean[]) {
            Boolean[] boxed = (Boolean[]) usedCatsObj;
            boolean[] arr = new boolean[boxed.length];
            for (int i = 0; i < boxed.length; i++) arr[i] = boxed[i] != null && boxed[i];
            return arr;
        }
        return new boolean[ScoreCategory.values().length];
    }
}
