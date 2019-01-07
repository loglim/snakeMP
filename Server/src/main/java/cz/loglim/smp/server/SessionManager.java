package cz.loglim.smp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class SessionManager {

    // Private
    private static Logger log;
    private static List<GameSession> gameSessionList;

    static void setLog(Logger log) {
        SessionManager.log = log;
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
        log.info(String.format("Created new game session (%d)", gameSessionList.size()));
        gameSessionList.add(s);
        return s;
    }

}
