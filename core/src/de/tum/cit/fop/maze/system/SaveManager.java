package de.tum.cit.fop.maze.system;


import java.io.*;


/**
 * Utility for saving and loading game state to disk.
 */
public class SaveManager {
    /**
     * Directory for save files.
     */
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
     * @param state    game state to serialize
     */
    public static void saveGame(String filename, GameState state) {
        java.io.File directory = new java.io.File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (state == null) {
            System.err.println("Game state is null");
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(SAVE_DIR + filename + ".sav"); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(state);
            System.out.println("Game state saved to " + SAVE_DIR + filename + ".sav");
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
        if (state == null) {
            System.err.println("Game state is null");
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(SAVE_DIR + "autosave" + ".sav"); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(state);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not save game: " + e.getMessage());
        }
    }

    public static boolean checkSaveExists(String filename) {
        try (FileInputStream fis = new FileInputStream(SAVE_DIR + filename + ".sav"); ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (GameState) ois.readObject() != null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Save doesnt exist: " + e.getMessage());
            return false;
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

    public static void saveInfo(boolean selectedScreen, int slot) {
        Boolean[] saveInfo = loadSaveInfo();
        saveInfo[slot] = selectedScreen;
        java.io.File directory = new java.io.File(SAVE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(SAVE_DIR + "metasave" + ".sav"); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(saveInfo);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not save info: " + e.getMessage());
        }
    }

    public static boolean getSaveInfo(int slotName){
        Boolean[] saveInfo = loadSaveInfo();
        return saveInfo[slotName];
    }

    private static Boolean[] loadSaveInfo() {
        try (FileInputStream fis = new FileInputStream(SAVE_DIR + "metasave" + ".sav"); ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Boolean[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load save information " + e.getMessage());
            return new Boolean[4];
        }
    }
}
