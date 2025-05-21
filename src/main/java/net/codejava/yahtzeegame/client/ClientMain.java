package net.codejava.yahtzeegame.client;

import net.codejava.yahtzeegame.network.NetworkClient;
import net.codejava.yahtzeegame.network.Message;
import net.codejava.yahtzeegame.client.ui.GameWindow;

public class ClientMain {
    public static void main(String[] args) {
        try {
            String serverIp = "localhost"; //"16.171.1.206"; //"16.171.1.206";
            int port = 5000;
            NetworkClient client = new NetworkClient(serverIp, port);

            GameWindow window = new GameWindow(client);
            window.setVisible(true);

            // Serverdan gelen mesajları oku
            new Thread(() -> {
                try {
                    while (true) {
                        Message msg = client.receiveMessage();
                        System.out.println("ClientMain: Server'dan mesaj alındı: " + msg.type + " | " + msg.data);
                        window.handleServerMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Thread kapanıyor! Bunu burada bilerek yaz
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
