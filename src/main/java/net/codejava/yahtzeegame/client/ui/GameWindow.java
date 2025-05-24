package net.codejava.yahtzeegame.client.ui;

import net.codejava.yahtzeegame.network.NetworkClient;
import net.codejava.yahtzeegame.network.Message;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * GameWindow – istemci tarafındaki ana pencere.
 */
public class GameWindow extends JFrame {

    //  Sabit referanslar
    private final NetworkClient client;            // TCP bağlantı sarmalayıcısı
    private final CardLayout   cardLayout = new CardLayout();
    private final JPanel       mainPanel  = new JPanel(cardLayout);

    // Alt paneller
    private final StartPanel startPanel = new StartPanel();
    private final PlayPanel  playPanel;            // run-time’da oluşturuluyor (client bağımlı)
    private final EndPanel   endPanel   = new EndPanel();

    private String playerName = "";                // Bu istemcinin takma adı

    // Ana pencereyi hazırlar ve bağlantı işleyicilerini tanımlar
    public GameWindow(NetworkClient client) {
        this.client   = client;
        this.playPanel = new PlayPanel(client);    // PlayPanel’in de socket’e ihtiyacı var

        initializeUI();    // Swing bileşenleri
        setupEventHandlers();  // Buton & ağ olayları
        mainPanel.setBackground(new Color(154, 154, 220));  // Lavanta tonu
    }

    // UI bileşenlerinin konum, başlık, boyut vb. ayarlarını yapar
    private void initializeUI() {
        setTitle("Yahtzee Online");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);               // Ekranın ortasına yerleştir

        // CardLayout içine panelleri yerleştir
        mainPanel.add(startPanel, "START");
        mainPanel.add(playPanel,  "PLAY");
        mainPanel.add(endPanel,   "END");

        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "START");       // Açılış ekranı
    }

    // Swing + socket olay dinleyicilerini tanımlar
    private void setupEventHandlers() {

        // StartPanel: Find Game
        startPanel.setConnectAction(e -> handleConnectAction());

        // EndPanel: Play Again
        endPanel.setReplayAction(e -> {
            try {
                Message replay = new Message("REPLAY_REQUEST");
                replay.put("playerName", playerName);
                client.sendMessage(replay);

                // Giriş ekranına döner
                cardLayout.show(mainPanel, "START");
                startPanel.reset();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Replay error: " + ex.getMessage());
            }
        });

        // EndPanel: Exit
        endPanel.setExitAction(e -> exitGame());
    }

    /** “Find Game” tıklanınca sunucuya JOIN_GAME gönderir. */
    private void handleConnectAction() {
        String name = startPanel.getPlayerName().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen bir isim girin!");
            return;
        }

        try {
            Message joinMsg = new Message("JOIN_GAME");
            joinMsg.put("playerName", name);
            System.out.println("[DEBUG] Sunucuya katılma isteği gönderiliyor...");
            client.sendMessage(joinMsg);

            playerName = name;
            startPanel.showWaiting();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Bağlantı hatası: " + ex.getMessage());
            startPanel.reset();
        }
    }

    /** Herhangi bir yerden giriş ekranına sıfırlar. */
    public void resetToStart() {
        startPanel.reset();
        cardLayout.show(mainPanel, "START");
    }

//       Sunucudan gelen mesajların Swing-thread üzerinde işlenmesi
    public void handleServerMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            switch (msg.type) {
                case "GAME_START":
                    // Sunucu kendi adını geri gönderir → sakla
                    playerName = (String) msg.get("playerName");
                    String opponentName = (String) msg.get("opponentName");

                    startGame(msg);   // PlayPanel’i hazırla

                    JOptionPane.showMessageDialog(this,
                            "Game started against " + opponentName +
                                    "\n\nYahtzee Rules:\n" +
                                    "- You can roll up to 2 times per turn\n" +
                                    "- Hold dice you want to keep between rolls\n" +
                                    "- Choose a category after final roll\n" +
                                    "- Each category can only be used once per game");
                    break;

                case "TURN_UPDATE":
                    playPanel.updateTurn(msg);
                    break;

                case "GAME_OVER":
                    playPanel.disableGameControls();
                    Map<String, Integer> results =
                            (Map<String, Integer>) msg.get("results");
                    String winner = (String) msg.get("winner");
                    endPanel.showResults(results, winner);
                    cardLayout.show(mainPanel, "END");
                    break;

                case "WAITING":
                    startPanel.showWaiting();
                    break;

                default:
                    startPanel.reset();   // Beklenmeyen mesaj -> sıfırla
            }
        });
    }

    // PlayPanel’i tazeleyip oyuna geçiş yapar.
    public void startGame(Message msg) {
        String myName       = (String) msg.get("playerName");
        String opponentName = (String) msg.get("opponentName");
        boolean amIP1       = (Boolean) msg.getOrDefault("youAreP1", false);

        setTitle("Yahtzee Online - You: " + myName + " vs Opponent: " + opponentName);

        playPanel.initForNewGame(myName, opponentName, amIP1);
        cardLayout.show(mainPanel, "PLAY");
    }

    // Uygulamayı kapatır; socket kapatma hatası önemli değil.
    private void exitGame() {
        try {
            client.close();
        } catch (Exception ignored) {
        }
        dispose();
    }
}
