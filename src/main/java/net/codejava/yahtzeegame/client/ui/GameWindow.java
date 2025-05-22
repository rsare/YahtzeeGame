package net.codejava.yahtzeegame.client.ui;

import net.codejava.yahtzeegame.network.NetworkClient;
import net.codejava.yahtzeegame.network.Message;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameWindow extends JFrame {
    private final NetworkClient client;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private final StartPanel startPanel = new StartPanel();
    private final PlayPanel playPanel;
    private final EndPanel endPanel = new EndPanel();

    private String playerName = "";

    public GameWindow(NetworkClient client) {
        this.client = client;
        this.playPanel = new PlayPanel(client);

        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        setTitle("Yahtzee Online");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        // Panelleri ana panele ekle
        mainPanel.add(startPanel, "START");
        mainPanel.add(playPanel, "PLAY");
        mainPanel.add(endPanel, "END");

        setContentPane(mainPanel);
        cardLayout.show(mainPanel, "START");
    }

    private void setupEventHandlers() {
        startPanel.setConnectAction(e -> handleConnectAction());

        endPanel.setReplayAction(e -> {                                         // 🔧 ADD
            try {
                Message replay = new Message("REPLAY_REQUEST");                 // yeni tip
                replay.put("playerName", playerName);
                client.sendMessage(replay);

                startPanel.showWaiting();                                       // eskisi gibi bekle
                cardLayout.show(mainPanel, "START");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Replay error: " + ex.getMessage());
            }
        });
        endPanel.setExitAction(e -> exitGame());
    }

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
            JOptionPane.showMessageDialog(this, "Bağlantı hatası: " + ex.getMessage());
            startPanel.reset();
        }
    }

    public void handleServerMessage(Message msg) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Sunucudan mesaj alındı: " + msg.type);

            switch (msg.type) {
                case "GAME_START":
                    playerName = (String) msg.get("playerName");
                    String opponentName = (String) msg.get("opponentName");
                    startGame(msg);
                    break;
                case "TURN_UPDATE":
                    playPanel.updateTurn(msg);
                    break;
                case "GAME_OVER":
                    playPanel.disableGameControls(); // Oyun bittiğinde tüm butonlar kapansın
                    Map<String, Integer> results = (HashMap<String, Integer>) msg.get("results");
                    String winner = (String) msg.get("winner");
                    endPanel.showResults(results, winner);
                    cardLayout.show(mainPanel, "END");
                    break;
                case "WAITING":
                    startPanel.showWaiting();
                    break;

                default:
                    startPanel.reset();
            }
        });
    }

    public void startGame(Message msg) {
        String myName = (String) msg.get("playerName");
        String opponentName = (String) msg.get("opponentName");
        System.out.println("Oyun başladı: " + myName + " vs " + opponentName);

        playPanel.initForNewGame(myName, opponentName);
        cardLayout.show(mainPanel, "PLAY");
    }

    private void exitGame() {
        try {
            client.close();
        } catch (Exception ignored) {
        }
        dispose();
    }
}