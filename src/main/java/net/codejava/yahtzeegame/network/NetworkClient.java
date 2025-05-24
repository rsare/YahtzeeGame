package net.codejava.yahtzeegame.network;

import java.io.*;
import java.net.Socket;

/**
 * NetworkClient – Swing istemcisinin sunucuyla TCP üzerinden mesaj alışverişini yöneten sınıf
 */
public class NetworkClient {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream  in;

    public NetworkClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        //SIRA ÖNEMLİ
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush();                     // Header’i hemen gönder
        this.in  = new ObjectInputStream(socket.getInputStream());
    }


    // Thread-safe gönderim: aynı anda birden çok Swing olayı çağırsa bile bozulma
    public synchronized void sendMessage(Message msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    // Sunucudan bir Message nesnesi okur; çağrı bloklanır
    public Message receiveMessage() throws IOException, ClassNotFoundException {
        Message msg = (Message) in.readObject();
        System.out.println("NetworkClient: Alındı → " + msg.type + " | " + msg.data);
        return msg;
    }


    // Bağlantıyı güvenli şekilde kapatır
    public void close() {
        try { in.close();    } catch (Exception ignored) {}
        try { out.close();   } catch (Exception ignored) {}
        try { socket.close();} catch (Exception ignored) {}
    }

    // Soketin hala bağlı olup olmadığını döndürür
    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
