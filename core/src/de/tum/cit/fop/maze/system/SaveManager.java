package de.tum.cit.fop.maze.system;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class SaveManager {
    private static final String SAVE_DIR = "saves/";

    private SaveManager() {
    }

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

    public static GameState loadGame(String filename) {
        try (FileInputStream fis = new FileInputStream(SAVE_DIR + filename + ".sav"); ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (GameState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not load game: " + e.getMessage());
            return null;
        }
    }
}

