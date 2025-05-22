package net.codejava.yahtzeegame.client;

import net.codejava.yahtzeegame.network.NetworkClient;
import net.codejava.yahtzeegame.network.Message;
import net.codejava.yahtzeegame.client.ui.GameWindow;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) {
        try {
            String serverIp = "13.60.44.150"; //"13.60.44.150";
            int port = 12345;
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
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    System.out.println("Connection closed or error occurred, stopping client receive thread.");
                }
            }).start();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
