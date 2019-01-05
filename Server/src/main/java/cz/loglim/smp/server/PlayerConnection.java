package cz.loglim.smp.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static cz.loglim.smp.dto.Protocol.TAG_GAME_OVER;
import static cz.loglim.smp.dto.Protocol.TAG_STATE_CHANGE;

class PlayerConnection {
    // Public

    // Private
    private GameSession gameSession;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private int id;

    private String name;
    private boolean identified;
    private boolean disconnected;

    PlayerConnection(GameSession gameSession, Socket socket) {
        this.gameSession = gameSession;
        this.socket = socket;
        System.out.println("> [OK] Accepted connection from " + this.socket.getInetAddress());

        // Setup communication channels (i/o)
        try {
            input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8),
                    true);
            disconnected = false;
        } catch (IOException e) {
            System.out.println("> [ERR] Cannot establish user connection!");
        }
    }

    void setIdentified() {
        identified = true;
    }

    void markDisconnected() {
        disconnected = true;
    }

    boolean isIdentified() {
        return identified;
    }

    boolean isDisconnected() {
        return disconnected;
    }

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String get() {
        if (disconnected) return null;

        String message = null;
        if (!socket.isClosed()) {
            try {
                message = input.readLine();
            } catch (IOException ignored) {
            }
        }
        return message;
    }

    void post(String message, boolean showOutput) {
        if (disconnected) return;

        if (showOutput) {
            System.out.println(String.format("> pID-%d: %s", id, message));
        }
        output.println(message);
    }

    void sendResults(Game game, List<Integer> scoreList) {
        if (disconnected) return;

        int score = game.getPlayerScore(id);
        int resPos = scoreList.indexOf(score) + 1;
        post(String.format("%s%s%d", TAG_STATE_CHANGE, TAG_GAME_OVER, resPos), true);
    }

    boolean checkConnection() {
        if (!socket.isConnected()) {
            disconnected = true;
        }
        return disconnected;
    }

}
