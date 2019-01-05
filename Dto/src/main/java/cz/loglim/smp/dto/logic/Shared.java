package cz.loglim.smp.dto.logic;

import java.util.Random;

class Shared {
    private static Random random = new Random();

    static Random getRandom() {
        return random;
    }
}
