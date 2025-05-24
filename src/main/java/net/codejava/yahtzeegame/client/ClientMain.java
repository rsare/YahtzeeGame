/*
 * CLIENT-SIDE ENTRY POINT
 */
package net.codejava.yahtzeegame.client;

import net.codejava.yahtzeegame.network.NetworkClient;
import net.codejava.yahtzeegame.network.Message;
import net.codejava.yahtzeegame.client.ui.GameWindow;

import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) {
        try {
            // ---------- CONNECTION SETUP ---------- //
            String serverIp = "13.60.44.150";  // Azure/AWS public IP of Yahtzee server
            int port = 12345;
            NetworkClient client = new NetworkClient(serverIp, port);

            // ---------- UI ---------- //
            GameWindow window = new GameWindow(client);
            window.setVisible(true);

            // ---------- ASYNC RECEIVE LOOP ---------- //
            new Thread(() -> {
                try {
                    while (true) {
                        Message msg = client.receiveMessage();
                        System.out.println("ClientMain: Received → " + msg.type + " | " + msg.data);
                        window.handleServerMessage(msg);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    // Connection dropped or user quit
                    e.printStackTrace();
                    System.out.println("Connection closed / receive thread stopped.");
                }
            }, "receive-thread").start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}