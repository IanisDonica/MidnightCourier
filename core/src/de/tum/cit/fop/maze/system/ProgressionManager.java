package de.tum.cit.fop.maze.system;

import de.tum.cit.fop.maze.system.progression.DrinkSpeedIUpgrade;
import de.tum.cit.fop.maze.system.progression.DrinkSpeedIIUpgrade;
import de.tum.cit.fop.maze.system.progression.HealthBoostIIUpgrade;
import de.tum.cit.fop.maze.system.progression.HealthBoostIIIUpgrade;
import de.tum.cit.fop.maze.system.progression.HealthUpgrade;
import de.tum.cit.fop.maze.system.progression.MasterUpgrade;
import de.tum.cit.fop.maze.system.progression.NewGlassesUpgrade;
import de.tum.cit.fop.maze.system.progression.PotholImunityUpgrade;
import de.tum.cit.fop.maze.system.progression.RegenUpgrade;
import de.tum.cit.fop.maze.system.progression.RootUpgrade;
import de.tum.cit.fop.maze.system.progression.SpeedIIUpgrade;
import de.tum.cit.fop.maze.system.progression.SpeedIIIUpgrade;
import de.tum.cit.fop.maze.system.progression.SpeedUpgrade;
import de.tum.cit.fop.maze.system.progression.StealthUpgrade;
import de.tum.cit.fop.maze.system.progression.Upgrade;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProgressionManager {
    private final Map<String, Upgrade> upgradesByName = new HashMap<>();
    private final Set<String> ownedUpgrades = new HashSet<>();
    private int points;

    public ProgressionManager() {
        registerDefaults();
    }

    public ProgressionManager(int startingPoints) {
        this.points = startingPoints;
        registerDefaults();
    }

    public ProgressionManager(Collection<? extends Upgrade> upgrades, int startingPoints) {
        this.points = startingPoints;
        for (Upgrade upgrade : upgrades) {
            upgradesByName.put(upgrade.getName(), upgrade);
        }
    }

    public boolean buyUpgrade(String upgradeName) {
        Upgrade upgrade = upgradesByName.get(upgradeName);
        if (upgrade == null) {
            return false;
        }
        if (ownedUpgrades.contains(upgradeName)) {
            return false;
        }
        if (!hasAllPrerequisites(upgrade)) {
            return false;
        }
        if (points < upgrade.getCost()) {
            return false;
        }
        points -= upgrade.getCost();
        ownedUpgrades.add(upgradeName);
        return true;
    }

    public boolean hasUpgrade(String upgradeName) {
        return ownedUpgrades.contains(upgradeName);
    }

    public boolean canPurchase(String upgradeName) {
        Upgrade upgrade = upgradesByName.get(upgradeName);
        if (upgrade == null) {
            return false;
        }
        if (ownedUpgrades.contains(upgradeName)) {
            return false;
        }
        return hasAllPrerequisites(upgrade) && points >= upgrade.getCost();
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        points += amount;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Map<String, Upgrade> getUpgrades() {
        return Collections.unmodifiableMap(upgradesByName);
    }

    public Set<String> getOwnedUpgrades() {
        return Collections.unmodifiableSet(ownedUpgrades);
    }

    public void setOwnedUpgrades(Collection<String> upgrades) {
        ownedUpgrades.clear();
        if (upgrades != null) {
            ownedUpgrades.addAll(upgrades);
        }
    }

    public Upgrade getUpgrade(String upgradeName) {
        return upgradesByName.get(upgradeName);
    }

    private boolean hasAllPrerequisites(Upgrade upgrade) {
        for (String prereq : upgrade.getPrerequisites()) {
            if (!ownedUpgrades.contains(prereq)) {
                return false;
            }
        }
        return true;
    }

    private void registerDefaults() {
        upgradesByName.put("root", new RootUpgrade());
        upgradesByName.put("speed", new SpeedUpgrade());
        upgradesByName.put("health", new HealthUpgrade());
        upgradesByName.put("stealth", new StealthUpgrade());
        upgradesByName.put("speed_2", new SpeedIIUpgrade());
        upgradesByName.put("health_2", new HealthBoostIIUpgrade());
        upgradesByName.put("speed_3", new SpeedIIIUpgrade());
        upgradesByName.put("health_3", new HealthBoostIIIUpgrade());
        upgradesByName.put("new_glasses", new NewGlassesUpgrade());
        upgradesByName.put("regen", new RegenUpgrade());
        upgradesByName.put("master", new MasterUpgrade());
        upgradesByName.put("drink_speed_1", new DrinkSpeedIUpgrade());
        upgradesByName.put("drink_speed_2", new DrinkSpeedIIUpgrade());
        upgradesByName.put("pothol_imunity", new PotholImunityUpgrade());
    }
}
