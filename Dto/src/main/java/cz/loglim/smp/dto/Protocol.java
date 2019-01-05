package cz.loglim.smp.dto;

public class Protocol {

    // Constants
    public static final int VICTORY_CONDITION_DEFAULT = 10;
    public static final int VICTORY_CONDITION_MIN = 4; // Should be "INITIAL_SIZE + 1"
    public static final int VICTORY_CONDITION_MAX = 100;
    public static final int INITIAL_SIZE_DEFAULT = 3;
    public static final int INITIAL_SIZE_MIN = 1;
    public static final int INITIAL_SIZE_MAX = 50; // Should be "VICTORY_CONDITION_MAX / 2"
    public static final int FOOD_LIMIT = 3;
    public static final int PLAYER_LIMIT_DEFAULT = 4;
    public static final int PLAYER_LIMIT_MIN = 1;
    public static final int PLAYER_LIMIT_MAX = 8;
    public static final int OBSTACLE_COUNT = 16;
    public static final int FOOD_SAFE_DISTANCE = 0;
    public static final int PLAYER_SAFE_DISTANCE = 5;
    public static final String TAG_IDENTIFICATION_REQUEST = "id_req";
    public static final String TAG_QUEUE_SIZE = "q_size";
    public static final String TAG_QUEUE_COMPLETE = "q_done";
    public static final String TAG_PLAYER_ID = "you_id";
    public static final String TAG_PLAYER_DATA = "p_data";
    public static final String TAG_PLAYER_DIRECTION = "p_dir";
    public static final String TAG_STATE_CHANGE = "st_chg";
    public static final String TAG_GAME_OVER = "p_won";

    public enum Phase {
        identification,
        queue,
        gameStart,
        gameInProgress,
        results,
        closed
    }

    private Phase phase;

    public Protocol(Phase initialPhase) {
        phase = initialPhase;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

}
