// src/main/java/net/codejava/yahtzeegame/network/MessageType.java
package net.codejava.yahtzeegame.network;

public enum MessageType {
    HELLO,      // Initial handshake
    MATCHED,    // Paired and game start
    ROLL,       // Dice roll event
    SELECT,     // Category selection event
    UPDATE,     // General update (not used right now)
    END,        // Game over / concede
    PING,     // (optional keep-alive)
    REPLAY_REQUEST   // 🔧 ADD – Play-Again isteği


}
