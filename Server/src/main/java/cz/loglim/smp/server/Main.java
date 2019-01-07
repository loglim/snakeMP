package cz.loglim.smp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static cz.loglim.smp.dto.Protocol.*;

public class Main {

    // Private
    private static Scanner scanner;
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("SnakeMP - Server application");
        printSeparator();
        scanner = new Scanner(System.in);

        // Allow server defaults adjustment
        GameSession.setPlayerLimit(
                chooseValue("player limit", PLAYER_LIMIT_DEFAULT, PLAYER_LIMIT_MIN, PLAYER_LIMIT_MAX));
        GameSession.setInitialSize(
                chooseValue("initial player size", INITIAL_SIZE_DEFAULT, INITIAL_SIZE_MIN, INITIAL_SIZE_MAX));
        GameSession.setTargetScore(
                chooseValue("target score", VICTORY_CONDITION_DEFAULT, VICTORY_CONDITION_MIN, VICTORY_CONDITION_MAX));

        // Decide whether to use console grid output
        if (yesNoChoice("Enable grid preview in console (y/n)?") == 'y') {
            GameSession.setShowGridInConsole();
        }

        // Set session manager log
        SessionManager.setLog(log);

        try {
            ServerSocket serverSocket = new ServerSocket(2018);
            log.info("> [OK] Server is running!");
            printSeparator();

            // Wait for connectionList
            while (!Thread.currentThread().isInterrupted()) {
                handleNextClient(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("Problem with opening socket: {}", e.toString());
        }

        log.info("> [OK] Server stopped!");
    }

    private static int chooseValue(String name, int defaultValue, int min, int max) {
        if (yesNoChoice(String.format("Use default %s (%d)? (y/n):", name, defaultValue)) == 'y') return defaultValue;

        int value;
        do {
            System.out.println(String.format("Enter new %s from range [%d; %d]:", name, min, max));
            while (!scanner.hasNextInt()) {
                scanner.next();
                System.out.println("Wrong input!");
                log.warn("Wrong input!");
            }
            value = scanner.nextInt();
        }
        while (value < min || value > max);
        log.info(String.format("New %s set to %d", name, value));
        printSeparator();
        return value;
    }

    private static char yesNoChoice(String question) {
        char choice;
        do {
            System.out.println(question);
            choice = scanner.next().charAt(0);
        } while (choice != 'y' && choice != 'n');
        printSeparator();
        return choice;
    }

    private static void printSeparator() {
        System.out.println("---");
    }

    private static void handleNextClient(Socket clientSocket) {
        // Request available session and pass it this client connection
        SessionManager.getAvailableSession().addPlayer(clientSocket);
    }
}
