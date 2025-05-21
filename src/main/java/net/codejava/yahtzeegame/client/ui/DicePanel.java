package net.codejava.yahtzeegame.client.ui;

import javax.swing.*;
import java.awt.*;

public class DicePanel extends JPanel {
    private int value = 1; // 1-6 arası zar değeri

    public DicePanel() {
        setPreferredSize(new Dimension(60, 60));
        setBackground(Color.WHITE);
    }

    public void setValue(int value) {
        this.value = value;
        repaint();
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Zarın kenarlığı
        g.setColor(Color.BLACK);
        g.fillRoundRect(5, 5, 50, 50, 15, 15);

        // Zarın içi
        g.setColor(Color.WHITE);
        g.fillRoundRect(8, 8, 44, 44, 10, 10);

        // Noktaların rengi
        g.setColor(Color.BLACK);

        // Zar noktalarının koordinatları
        int[][] dots = {
                {}, // 0
                {1}, // 1
                {0, 2}, // 2
                {0, 1, 2}, // 3
                {0, 2, 3, 5}, // 4
                {0, 1, 2, 3, 5}, // 5
                {0, 1, 2, 3, 4, 5} // 6
        };

        int[][] positions = {
                {30, 30}, // orta
                {15, 15}, // sol üst
                {45, 45}, // sağ alt
                {45, 15}, // sağ üst
                {30, 45}, // orta alt
                {15, 45}, // sol alt
                {30, 15}  // orta üst
        };

        // Nokta pozisyonları:
        // 0: sol üst, 1: orta, 2: sağ alt, 3: sağ üst, 4: orta üst, 5: sol alt

        // Klasik zar dizilişi için:
        switch (value) {
            case 1:
                drawDot(g, 30, 30);
                break;
            case 2:
                drawDot(g, 17, 17);
                drawDot(g, 43, 43);
                break;
            case 3:
                drawDot(g, 17, 17);
                drawDot(g, 30, 30);
                drawDot(g, 43, 43);
                break;
            case 4:
                drawDot(g, 17, 17);
                drawDot(g, 43, 17);
                drawDot(g, 17, 43);
                drawDot(g, 43, 43);
                break;
            case 5:
                drawDot(g, 17, 17);
                drawDot(g, 43, 17);
                drawDot(g, 30, 30);
                drawDot(g, 17, 43);
                drawDot(g, 43, 43);
                break;
            case 6:
                drawDot(g, 17, 17);
                drawDot(g, 43, 17);
                drawDot(g, 17, 30);
                drawDot(g, 43, 30);
                drawDot(g, 17, 43);
                drawDot(g, 43, 43);
                break;
        }
    }

    private void drawDot(Graphics g, int x, int y) {
        g.fillOval(x - 5, y - 5, 10, 10);
    }
}
