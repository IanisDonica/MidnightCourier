package de.tum.cit.fop.maze.system;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Utility for saving and loading game state to disk.
 */
public class SaveManager {
    /** Directory for save files. */
    private static final String SAVE_DIR = "saves/";

    /**
     * Prevents instantiation of utility class.
     */
    private SaveManager() {
    }

    /**
     * Saves a game state under the given filename.
     *
     * @param filename base filename without extension
     * @param state game state to serialize
     */
    public static void saveGame(String filename, GameState state) {
        java.io.File directory = new java.io.File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(SAVE_DIR + filename + ".sav"); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not save game: " + e.getMessage());
        }
    }

    /**
     * Saves a game state to the default autosave file.
     *
     * @param state game state to serialize
     */
    public static void saveGame(GameState state) {
        java.io.File directory = new java.io.File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(SAVE_DIR + "autosave" + ".sav"); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not save game: " + e.getMessage());
        }
    }

    /**
     * Loads a game state from the given filename.
     *
     * @param filename base filename without extension
     * @return loaded game state, or {@code null} if loading fails
     */
    public static GameState loadGame(String filename) {
        try (FileInputStream fis = new FileInputStream(SAVE_DIR + filename + ".sav"); ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load game: " + e.getMessage());
            return null;
        }
    }
}
