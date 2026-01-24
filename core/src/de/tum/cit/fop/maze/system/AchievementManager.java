package de.tum.cit.fop.maze.system;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class AchievementManager {
    // just a template system, feel free to change it!
    // used it to test my interface
    List<Achievement> achievements;

    public AchievementManager(){
        achievements = new ArrayList<Achievement>();

    }

    public List<Achievement> getAchievements(){
        return achievements;
    }
}
