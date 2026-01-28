package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;
import de.tum.cit.fop.maze.screen.AchievementPopupScreen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AchievementManager {
    private static final String FILE_PATH = "assets/data/achievements.json";
    private static final Map<String, Achievement> ACHIEVEMENTS = new LinkedHashMap<>();
    private static boolean initialized = false;
    private static AchievementPopupScreen popupScreen;

    // The AchievementManager isnt tied to a game or a screen, its initialized upon the start of the gamea and always the same one
    public static void init() {

        // In theory shouldn't happen, but just in case
        if (initialized) {
            return;
        }
        initialized = true;
        load();
        if (ACHIEVEMENTS.isEmpty()) {
            seedDefaults();
            save();
        }
    }

    public static List<Achievement> getAchievements() {
        init();
        return new ArrayList<>(ACHIEVEMENTS.values());
    }

    public static Achievement getAchievement(String id) {
        init();
        return ACHIEVEMENTS.get(id);
    }

    public static void incrementProgress(String id, int amount) {
        init();
        Achievement achievement = ACHIEVEMENTS.get(id);
        if (achievement == null) {
            return;
        }
        boolean wasUnlocked = achievement.isUnlocked();
        achievement.incrementProgress(amount);
        if (!wasUnlocked && achievement.isUnlocked()) {
            showPopup(achievement);
        }
        save();
    }

    public static void resetAll() {
        init();
        for (Achievement achievement : ACHIEVEMENTS.values()) {
            achievement.setProgress(0);
            achievement.setUnlocked(false);
        }
        save();
    }

    public static void setPopupScreen(AchievementPopupScreen screen) {
        popupScreen = screen;
    }

    private static void showPopup(Achievement achievement) {
        if (popupScreen != null) {
            popupScreen.showPopup(achievement.getName());
        }
    }


    // Reads the JSON file and loads it's content in the Manager class
    private static void load() {
        try {
            FileHandle file = Gdx.files.local(FILE_PATH);
            if (!file.exists()) {
                // No JSON file so nothing to laod
                // this will be the case on first load.
                return;
            }
            JsonValue root = new JsonReader().parse(file.readString("UTF-8"));
            ACHIEVEMENTS.clear();
            for (JsonValue child = root.child; child != null; child = child.next) {
                Achievement achievement = parseAchievement(child);
                if (achievement != null && achievement.getId() != null) {
                    ACHIEVEMENTS.put(achievement.getId(), achievement);
                }
            }
        } catch (Exception e) {
            System.err.println("can't not load achievements: " + e.getMessage());
        }
    }

    private static Achievement parseAchievement(JsonValue value) {
        if (value == null) {
            return null;
        }
        Achievement achievement = new Achievement();
        achievement.setId(value.getString("id", null));
        achievement.setName(value.getString("name", "Unnamed"));
        achievement.setDescription(value.getString("description", ""));
        achievement.setTarget(value.getInt("target", 1));
        achievement.setProgress(value.getInt("progress", 0));
        achievement.setUnlocked(value.getBoolean("unlocked", achievement.isUnlocked()));
        return achievement;
    }

    private static void save() {
        try {
            Json json = new Json();
            json.setTypeName(null); // Otherwise it'll add a ugly class atribute in the JSON file
            json.setOutputType(JsonWriter.OutputType.json);
            List<Achievement> list = new ArrayList<>(ACHIEVEMENTS.values());
            FileHandle file = Gdx.files.local(FILE_PATH);
            file.writeString(json.prettyPrint(list), false, "UTF-8");
        } catch (Exception e) {
            System.err.println("Can't save achivments: " + e.getMessage());
        }
    }

    private static void seedDefaults() {
        Achievement deliveries = new Achievement(
                "complete_100_deliveries",
                "Certified Delivery Man",
                "Complete 100 deliveries",
                100
        );
        ACHIEVEMENTS.put(deliveries.getId(), deliveries);
        Achievement firstDelivery = new Achievement(
                "first_delivery",
                "First delivery",
                "Complete your first delivery",
                1
        );
        ACHIEVEMENTS.put(firstDelivery.getId(), firstDelivery);
        Achievement firstArrest = new Achievement(
                "first_time_for_everything",
                "First time for everythihing",
                "Get arrested by a police officer for the first time",
                1
        );
        ACHIEVEMENTS.put(firstArrest.getId(), firstArrest);
        Achievement firstUpgrade = new Achievement(
                "first_upgrade",
                "First upgrade",
                "Buy your first upgrade",
                1
        );
        ACHIEVEMENTS.put(firstUpgrade.getId(), firstUpgrade);
        Achievement masteryUpgrade = new Achievement(
                "mastery_upgrade",
                "Deep pockets",
                "Purchase the mastery upgrade",
                1
        );
        ACHIEVEMENTS.put(masteryUpgrade.getId(), masteryUpgrade);
        Achievement thirdStrike = new Achievement(
                "third_strike_and_out",
                "Three strike rule",
                "Get arrested 3 times",
                3
        );
        ACHIEVEMENTS.put(thirdStrike.getId(), thirdStrike);
        Achievement fifthLevel = new Achievement(
                "finish_level_5",
                "Vacation Money",
                "Compled the Campaign",
                1
        );
        ACHIEVEMENTS.put(fifthLevel.getId(), fifthLevel);
        Achievement germanEngineering = new Achievement(
                "german_engineering",
                "German Engineering",
                "Get run over by a BMW",
                1
        );
        ACHIEVEMENTS.put(germanEngineering.getId(), germanEngineering);
    }
}
