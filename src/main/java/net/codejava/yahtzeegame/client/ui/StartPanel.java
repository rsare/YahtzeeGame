package net.codejava.yahtzeegame.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * StartPanel – oyuncunun adını girip “Find Game” butonuna basarak eşleşme kuyruğuna katıldığı açılış ekranı
 */
public class StartPanel extends JPanel {

    //Swing bileşenleri
    private final JLabel lblWelcome = new JLabel("Welcome to Online Yahtzee!", SwingConstants.CENTER);
    private final JTextField txtPlayerName = new JTextField(15);
    private final JButton btnConnect = new JButton("Find Game");
    private final JLabel lblStatus = new JLabel(" ", SwingConstants.CENTER);


    //  GridBagLayout kullanarak form oluşturma

    public StartPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 8, 12);

        // Başlangıçta buton ve text-field aktif
        btnConnect.setEnabled(true);
        txtPlayerName.setEnabled(true);

        // Yazı tipleri & renkler
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 26));
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 15));
        lblStatus.setForeground(Color.BLUE);

        // Başlık
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(lblWelcome, gbc);

        // Your Name etiketi + text field
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Your Name: "), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(txtPlayerName, gbc);

        //Find Game butonu
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnConnect, gbc);

        // Durum etiketi
        gbc.gridy = 3;
        add(lblStatus, gbc);
    }

//       GENEL KULLANIM METOTLARI

    //Find Game butonuna tıklanınca tetiklenecek ActionListener’ı ayarlar
    public void setConnectAction(ActionListener listener) {
        btnConnect.addActionListener(listener);
    }

    // Kullanıcının girdiği ad
    public String getPlayerName() {
        return txtPlayerName.getText().trim();
    }

    // Eşleşme beklerken kullanıcıya bilgi verir ve kontrolleri kilitler
    public void showWaiting() {
        lblStatus.setText("Waiting for another player to join...");
        btnConnect.setEnabled(false);
        txtPlayerName.setEnabled(false);
    }

    // Paneli varsayılan duruma sıfırlar
    public void reset() {
        lblStatus.setText(" ");
        btnConnect.setEnabled(true);
        txtPlayerName.setEnabled(true);
        txtPlayerName.setText("");
        txtPlayerName.requestFocusInWindow();  // İmleci input’a taşı
    }
}
