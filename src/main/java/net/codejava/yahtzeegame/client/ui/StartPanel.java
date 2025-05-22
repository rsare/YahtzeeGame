package net.codejava.yahtzeegame.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class StartPanel extends JPanel {
    private JLabel lblWelcome = new JLabel("Welcome to Online Yahtzee!", SwingConstants.CENTER);
    private JTextField txtPlayerName = new JTextField(15);
    private JButton btnConnect = new JButton("Find Game");
    private JLabel lblStatus = new JLabel(" ", SwingConstants.CENTER);

    public StartPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 8, 12);

        btnConnect.setEnabled(true);         // BAŞLANGIÇTA AKTİF!
        txtPlayerName.setEnabled(true);      // BAŞLANGIÇTA AKTİF!

        lblStatus.setText(" ");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 26));
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 15));
        lblStatus.setForeground(Color.BLUE);

        // Sıra: Başlık
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblWelcome, gbc);

        // Sıra: Oyuncu adı
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Your Name: "), gbc);

        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        add(txtPlayerName, gbc);

        // Sıra: Bağlan butonu
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        add(btnConnect, gbc);

        // Sıra: Durum/bilgi
        gbc.gridy = 3;
        add(lblStatus, gbc);
    }

    /**
     * Bağlan butonuna tıklanınca çalışacak action.
     */
    public void setConnectAction(ActionListener listener) {
        btnConnect.addActionListener(listener);
    }

    /**
     * Kullanıcı adını döndürür.
     */
    public String getPlayerName() {
        return txtPlayerName.getText().trim();
    }

    /**
     * Bağlan/oyun arama sırasında kullanıcıyı bilgilendirir.
     */
    public void showWaiting() {
        lblStatus.setText("Waiting for another player to join...");
        btnConnect.setEnabled(false);
        txtPlayerName.setEnabled(false);
    }

    /**
     * Durumu sıfırlar (ör. tekrar başlatınca)
     */
    public void reset() {
        lblStatus.setText(" ");
        btnConnect.setEnabled(true);
        txtPlayerName.setEnabled(true);
        txtPlayerName.setText("");
        txtPlayerName.requestFocusInWindow();  // İmleci aktif yap
    }




}
