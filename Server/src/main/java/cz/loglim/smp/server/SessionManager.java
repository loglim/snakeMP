package cz.loglim.smp.server;

import java.util.ArrayList;
import java.util.List;

class SessionManager {

    // Private
    private static List<GameSession> gameSessionList;

    private SessionManager() {
    }

    static GameSession getAvailableSession() {
        if (gameSessionList == null) {
            gameSessionList = new ArrayList<>();
        }

        // Try to find an existing session with an empty player slot
        for (GameSession gameSession : gameSessionList) {
            if (!gameSession.isFull()) {
                return gameSession;
            }
        }

        // Otherwise create a new session and return its instance
        GameSession s = new GameSession(gameSessionList.size() + 1);
        gameSessionList.add(s);
        return s;
    }

}
