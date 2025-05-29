Play classic Yahtzee with a friend over the network.  Built with Java Swing for the UI and raw TCP sockets for real-time communication.


Table of Contents
1) Features
2) Quick Start
3) Project Structure
4) Running the Server
5) Running the Client
6) Gameplay & Rules
7) Tech Stack
8) Roadmap
9) Contributing
10) License

    
Features
- Two-player online play – automatic matchmaking with a waiting queue.
- Real-time turns – dice rolls, holds and category selection are sent instantly.
- Surrender & Replay – give up if you are losing, or start a new round without restarting the app.
- Score board – live table that stays in sync on both screens.
- Pure Java – no external game engines or frameworks; just Swing & java.net.

Quick Start
```
# clone the repo
$ git clone https://github.com/your‑username/yahtzee-online.git
$ cd yahtzee-online

# build everything (requires Maven 3.8+ & JDK 17+)
$ mvn clean package
```

1  – Run the server
```
$ java -jar server/target/yahtzee-server.jar    # default: port 12345
```
2  – Run two clients (locally or on remote PCs)
```
$ java -jar client/target/yahtzee-client.jar --host <SERVER_IP> --port 12345
```

Enter a nickname, click Find Game and wait until another player connects.

Project Structure:
```
├─ client/
│  └─ src/main/java/net/codejava/yahtzeegame/client/
│     ├─ ui/          ← Swing panels (Start, Play, End)
│     ├─ model/       ← Score logic & helpers
│     └─ ClientMain.java
├─ server/
│  └─ src/main/java/net/codejava/yahtzeegame/server/
│     ├─ ServerMain.java   ← accepts sockets, manages queue
│     ├─ GameSession.java  ← single match thread
│     └─ ClientHandler.java
└─ network/  ← common serialisable Message + enums
```

Running the Server
Edit ServerMain.PORT if you need a different port.  Deploy on an on‑prem machine, an AWS EC2, Azure VM – anywhere with a public IP.

Running the Client
You only need JRE 17+.  Pass server address via CLI or edit ClientMain:
```
String serverIp = "13.60.44.150";  // change to your server
int     port    = 12345;
```
When both players join, the server sends a GAME_START message and the UI switches to the board.

Gameplay & Rules
- Standard Yahtzee rules: up to 3 rolls per turn, hold dice you like, then choose one unused category.
- Upper‑section bonus – 35 pts if sum ≥ 63.
- Surrender button ends the game instantly and awards victory to the opponent.
- Play Again – both players must click; when both REPLAY_REQUESTs arrive the same sockets are reused for a fresh 13‑round match.





